(ns protocol55.styleguide.main.build
  (:require cljs.main
            figwheel.main
            [cljs.build.api :as api]
            [clojure.java.io :as io]
            [protocol55.styleguide.main.config :refer [*config*]]
            [protocol55.styleguide.main.stylesheets :as stylesheets]
            [protocol55.styleguide.main.server.ring :refer [index-html]]))

(defn for-dev [options]
  (let [fw-opts   (pr-str {:ring-stack 'protocol55.styleguide.main.server.ring/styleguide-stack
                           :css-dirs   (stylesheets/css-dirs (:css options))})
        cljs-opts (pr-str {:cache-analysis     false
                           :output-to          "target/public/cljs-out/main.js"
                           :output-dir         "target/public/cljs-out/"})
        fw-args ["-fwo" fw-opts "-co" cljs-opts "-c" "protocol55.styleguide.main.client.core" "-r"]]
    (binding [*config* options]
      (apply figwheel.main/-main fw-args))))

(defn for-dist
  [{:keys [css theme-css build-dir] :as options}]
  (let [cljs-opts {:main              'protocol55.styleguide.main.client.core
                   :cache-analysis     false
                   :optimizations      :advanced
                   :output-to          (str build-dir "cljs-out/main.js")}]

    (let [index-file (io/file (str build-dir "index.html"))]
      (io/make-parents index-file)
      (io/copy (index-html theme-css css) index-file))

    (stylesheets/copy-css! build-dir theme-css css)

    (binding [*config* options]
      (api/build cljs-opts))

    (println (format "[Styleguide] Build output to %s." build-dir))
    (System/exit 0)))
