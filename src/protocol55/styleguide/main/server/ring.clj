(ns protocol55.styleguide.main.server.ring
  (:require figwheel.server.ring
            [clojure.java.io :as io]
            [protocol55.styleguide.main.config :refer [*config*]]
            [protocol55.styleguide.main.stylesheets :as stylesheets]))

(defmulti styleguide-handler
  (fn [{:keys [root] :as config} {:keys [uri] :as request}]
    (if (= root "/")
      uri
      (subs uri (count root)))))

(defn index-html [theme-css css]
  (format
    (slurp (io/resource "public/protocol55/styleguide/content/index.html"))
    (stylesheets/css-links theme-css css)))

(defmethod styleguide-handler :default
  [{:keys [theme-css css] :as config} _]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (index-html theme-css css)})

(defn styleguide-middleware
  ([handler]
   (styleguide-middleware handler *config*))
  ([handler {:keys [root css theme-css] :as config}]
   (fn [{:keys [uri] :as request}]
     (cond
       (not (clojure.string/starts-with? uri root))
       (handler request)

       :else
       (styleguide-handler config request)))))

(defn default-handler [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Navigate to your styleguide root."})

(defn styleguide-stack [handler options]
  (-> (or handler default-handler)
      (styleguide-middleware)
      (figwheel.server.ring/default-stack options)))
