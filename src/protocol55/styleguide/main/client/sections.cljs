(ns protocol55.styleguide.main.client.sections
  (:require [protocol55.styleguide.alpha :as sg]))

(defn tertiary-sections-seq [reference styleguide]
  (let [ref-key (sg/reference-key reference)]
    ;; only return sections for secondary depths
    (when (> (count ref-key) 1)
      (->> styleguide
           (sg/sections-seq (take 2 ref-key) 2 sg/max-depth-default)
           (drop 1)))))

(defn secondary-sections-seq [reference styleguide]
  (let [ref-key (sg/reference-key reference)]
    (->> styleguide
         (sg/sections-seq (take 1 ref-key) 2)
         (drop 1))))

(defn primary-sections-seq [styleguide]
  (sg/sections-seq [] 1 styleguide))
