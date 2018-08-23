(ns protocol55.styleguide.main
  (:require [clojure.tools.cli :as cli]
            protocol55.styleguide.main.server.ring
            protocol55.styleguide.main.kss-reload
            protocol55.styleguide.main.overview-reload
            [protocol55.styleguide.main.build :as build]))

(defn conj-multi-option [m k v]
  (update m k (fnil conj #{}) v))

(def cli-options
  [["-s" "--source DIR" "Source directory to recursively parse for KSS comments"]
   ["-m" "--mask EXT" "Extension mask for detecting files containing KSS comments"
    :default nil
    :parse-fn #(if (clojure.string/starts-with? % ".") (subs % 1) %)
    :assoc-fn conj-multi-option]
   ["-r" "--root URI" "Root uri of the style guide"
    :default "/"
    :parse-fn #(if (clojure.string/starts-with? % "/") % (str "/" %))]
   [nil "--css URL" "URL of a CSS file to include in the style guide"
    :default nil
    :assoc-fn conj-multi-option]
   [nil "--theme-css URL" "URL of a CSS file to include to override the default style guide theme"
    :default nil
    :assoc-fn conj-multi-option]
   [nil "--build-dir DIR" "Builds an optimized version of the style guide into DIR"
    :parse-fn #(if (clojure.string/ends-with? % "/") % (str % "/"))]
   [nil "--overview FILE" "File name of the overview's Markdown file"
    :default nil
    :validate [#(clojure.string/ends-with? % ".md") "Must be a markdown file"]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Usage: clj -m protocol55.styleguide.main [options]"
        ""
        "Options:"
        options-summary]
       (clojure.string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      :else
      {:options options})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (if (:build-dir options)
        (build/for-dist options)
        (build/for-dev options)))))
