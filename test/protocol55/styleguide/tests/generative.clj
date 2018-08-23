(ns protocol55.styleguide.tests.generative
  (:require protocol55.styleguide.specs.alpha
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s])
    (:use clojure.test))

(def check-opts {:clojure.spec.test.check/opts {:num-tests 50 :max-size 50}})

(st/instrument)

(defn passes-check? [sym]
  (= 1 (:check-passed (st/summarize-results (st/check sym check-opts)))))

(deftest ^:check test-reference-key-check
  (is (passes-check? `protocol55.styleguide.alpha/reference-key)))

(deftest ^:check test-styleguide-check
  (is (passes-check? `protocol55.styleguide.alpha/styleguide)))

(deftest ^:check test-weighted-reference-key-check
  (is (passes-check? `protocol55.styleguide.alpha/weighted-reference-key)))

(deftest ^:check test-weighted-reference-key-compare-check
  (is (passes-check? `protocol55.styleguide.alpha/weighted-reference-key-compare)))

(deftest ^:check test-build-weight-map
  (is (passes-check? `protocol55.styleguide.alpha/build-weight-map)))

(deftest ^:check test-base-ref-depth-check
  (is (passes-check? `protocol55.styleguide.alpha/base-ref-depth)))

(deftest ^:check test-assign-section-ids-check
  (is (passes-check? `protocol55.styleguide.alpha/assign-section-ids)))

(deftest ^:check test-sections-seq-check
  (is (passes-check? `protocol55.styleguide.alpha/sections-seq)))
