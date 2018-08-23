(ns protocol55.styleguide.main.stylesheets
  (:require [figwheel.main.watching :as fww]
            [protocol55.kss.core :as kss]
            [clojure.java.io :as io]))

(defn get-suffixes [mask]
  (or mask #{"css" "less" "sass" "scss" "styl" "stylus"}))

(defn get-docs [{:keys [source mask] :as config}]
  (let [suffixes (get-suffixes mask)]
    (->> (file-seq (clojure.java.io/file source))
         (filter #(and (.isFile %) (suffixes (fww/file-suffix %))))
         (mapcat kss/parse-file)
         vec)))

(defn public-resource [path]
  (io/resource (str "public/" path)))

(defn css-dirs [css]
  (->> (map public-resource css)
       (remove nil?)
       (mapv #(.getParent (io/file %)))))

(defn css-files [css]
  (->> (map public-resource css)
       (remove nil?)
       (mapv io/file)))

(defn public-parts [f]
  "Returns a vector of [public-path file-name]."
  (let [proc-dir (System/getProperty "user.dir")
        file-dir (.getParent f)
        file-name (.getName f)
        rel-dir (subs file-dir (inc (count proc-dir)))
        public-dir (clojure.string/replace-first rel-dir #"^(.*?)\/?public\/" "")]
    [public-dir file-name]))

(def default-theme-css
  ["https://fonts.googleapis.com/css?family=Roboto+Slab:300,400,700"
   "/protocol55/styleguide/content/default_theme/default_theme.css"])

(defn css-refs [theme-css css]
  (->> (into #{} (concat css (or theme-css default-theme-css)))
       (map (fn [css-ref]
              (if (public-resource css-ref)
                (if (clojure.string/starts-with? css-ref "/")
                  (str "./" (subs css-ref 1))
                  css-ref)
                css-ref)))))

(defn css-links [theme-css css]
  (->> (css-refs theme-css css)
       (map #(format "<link href=\"%s\" rel=\"stylesheet\">" %))
       (clojure.string/join "\n    ")))

(defn copy-css! [build-dir theme-css css]
  (let [files (->> (css-refs theme-css css) (css-files))]
    (doseq [file files]
      (let [[file-path file-name] (public-parts file)
            output-file (io/file build-dir file-path file-name)]
        (io/make-parents build-dir file-path file-name)
        (io/copy file (io/file build-dir file-path file-name))))))
