(ns protocol55.styleguide.main.kss-reload
  #?(:cljs
     (:require ajax.core
               [cljs.reader :as edn]
               [protocol55.styleguide.alpha :as sg]
               [protocol55.styleguide.main.client.state :refer [state]]))
  #?(:cljs
     (:require-macros [protocol55.styleguide.main.kss-reload :refer [start inject-docs]]))
  #?(:clj
     (:require [figwheel.main.watching :as fww]
               [figwheel.main.css-reload :refer [client-eval]]
               [protocol55.styleguide.main.stylesheets :as stylesheets]
               [protocol55.styleguide.main.config :refer [*config*]]
               [protocol55.styleguide.main.server.ring :as ring-server]
               [protocol55.kss.core :as kss])))

#?(:clj
   (do

(defmethod ring-server/styleguide-handler "/api/docs"
  [config _]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (pr-str (stylesheets/get-docs config))})

(defn reload-docs [docs]
  (client-eval
    (format "protocol55.styleguide.main.kss_reload.reload_kss_docs_remote(%s);"
            (-> docs vec pr-str pr-str))))

(defn start* [{:keys [source mask build-dir] :as config}]
  (when-not build-dir
    (fww/add-watch!
      [::watcher source]
      {:paths [source]
       :filter (fww/suffix-filter (stylesheets/get-suffixes mask))
       :handler (fww/throttle
                  50
                  (bound-fn [evts]
                    (when-some [files (not-empty (mapv :file evts))]
                      (reload-docs (mapcat kss/parse-file files)))))})))

(ring-server/reg-reloader start*)

(defmacro start
  "Starts the figwheel watcher to live-reload kss docs. Disabled when the build-dir is present."
  []
  (start* *config*) nil)

(defmacro inject-docs
  "If build-dir option is present this inlines the kss docs. Otherwise, does a request for them."
  []
  (if (:build-dir *config*)
    `(protocol55.styleguide.main.kss-reload/reload-kss-docs-remote ~(pr-str (stylesheets/get-docs *config*)))
    (let [endpoint (str (:root *config*) "api/docs")]
      `(ajax.core/GET ~endpoint {:handler protocol55.styleguide.main.kss-reload/reload-kss-docs-remote}))))

))

#?(:cljs
   (do

(defn set-docs! [docs]
  (swap! state update :styleguide sg/add docs))

(defn ^:export reload-kss-docs-remote [docs]
  (-> (edn/read-string docs)
      set-docs!))

(inject-docs)

))
