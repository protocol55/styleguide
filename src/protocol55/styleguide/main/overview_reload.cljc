(ns protocol55.styleguide.main.overview-reload
  #?(:cljs
     (:require ajax.core
               [protocol55.styleguide.main.client.state :refer [state]]))
  #?(:cljs
     (:require-macros [protocol55.styleguide.main.overview-reload :refer [start inject-overview-html]]))
  #?(:clj
     (:require [clojure.java.io :as io]
               [markdown.core :refer [md-to-html-string]]
               [figwheel.main.watching :as fww]
               [figwheel.main.css-reload :refer [client-eval]]
               [protocol55.styleguide.main.server.ring :as ring-server]
               [protocol55.styleguide.main.config :refer [*config*]])))

#?(:clj
   (do

(defn get-html [file-or-path]
  (cond
    (string? file-or-path)
    (recur (io/file file-or-path))

    (instance? java.io.File file-or-path)
    (md-to-html-string (slurp file-or-path))))

(defmethod ring-server/styleguide-handler "/api/overview-html"
  [{:keys [overview] :as conifig} _]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (get-html overview)})

(defn reload-overview [html]
  (client-eval
    (format "protocol55.styleguide.main.overview_reload.reload_overview_html_remote(%s);"
            (-> html pr-str))))

(defn start* [{:keys [overview build-dir] :as config}]
  (when overview
    (when-not build-dir
      (let [overview-file (.getAbsoluteFile (io/file overview))
            overview-dir (.getParent overview-file)]
        (fww/add-watch!
          [::watcher ::overview]
          {:paths [overview-dir]
           :filter (fww/suffix-filter #{"md"})
           :handler (fww/throttle
                      50
                      (bound-fn [evts]
                        (when-some [files (not-empty (mapv :file evts))]
                          (reload-overview (get-html overview-file)))))})))))

(ring-server/reg-reloader start*)

(defmacro start
  "Starts the figwheel watcher to live-reload the overview. Disabled when the build-dir is present."
  []
  (start* *config*) nil)

(defmacro inject-overview-html
  "If build-dir option is present this inlines the overview. Otherwise, does a request for it."
  []
  (when (:overview *config*)
    (if (:build-dir *config*)
      `(protocol55.styleguide.main.overview-reload/set-overview-html! ~(get-html (:overview *config*)))
      (let [endpoint (str (:root *config*) "api/overview-html")]
        `(ajax.core/GET ~endpoint {:handler protocol55.styleguide.main.overview-reload/reload-overview-html-remote})))))

))

#?(:cljs
   (do

(defn set-overview-html! [overview-html]
  (swap! state assoc :overview-html overview-html))

(defn ^:export reload-overview-html-remote [overview-html]
  (set-overview-html! overview-html))

(inject-overview-html)

))
