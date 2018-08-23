(ns protocol55.styleguide.specs.alpha
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [protocol55.kss.specs :as kss-specs]))

;; reference / section

(s/def ::string-part (s/and string?
                            #(not (clojure.string/blank? %))
                            #(not (re-find #"^\d+$" %))))
(s/def ::int-part (s/and int? pos?))
(s/def ::part (s/or :s ::string-part :n ::int-part))
(s/def ::strict-string-reference-key
  (s/coll-of ::string-part :kind vector? :min-count 1))
(s/def ::strict-int-reference-key
  (s/coll-of ::int-part :kind vector? :min-count 1))

(defn part-gen []
  (s/gen ::part))

(defn reference-key-gen []
  (gen/one-of [(s/gen ::strict-string-reference-key)
               (s/gen ::strict-int-reference-key)]))

(defn reference-keys-gen []
  (gen/one-of [(gen/vector (s/gen ::strict-string-reference-key))
               (gen/vector (s/gen ::strict-int-reference-key))]))

(defn reference-keys-pair-gen []
  (gen/one-of [(gen/vector (s/gen ::strict-string-reference-key) 2)
               (gen/vector (s/gen ::strict-int-reference-key) 2)]))

(defn reference-gen []
  (gen/fmap
    #(clojure.string/join "." %)
    (reference-key-gen)))

(defn section-id-gen []
  (gen/fmap
    #(clojure.string/join "." %)
    (s/gen (s/coll-of ::int-part :kind vector? :min-count 1))))

(s/def ::reference-key (s/spec vector? :gen reference-key-gen))
(s/def ::section-id (s/spec string? :gen section-id-gen))
(s/def ::reference (s/spec ::kss-specs/reference :gen reference-gen))





;; sections

(defn weighted-reference-keys-pair-gen []
  (gen/fmap
    #(map (partial mapv (partial vector 0.0)) %)
    (reference-keys-pair-gen)))

(s/def ::weight-map (s/map-of (s/and int? pos?) (s/map-of ::part ::kss-specs/weight)))
(s/def ::weighted-reference-key (s/coll-of (s/tuple ::kss-specs/weight ::part)))
(s/def ::section (s/merge ::kss-specs/kss (s/keys :req-un [::section-id ::reference-key])))
(s/def ::sections (s/coll-of ::section))





;; styleguide

(defn strict-docs-gen []
  (gen/fmap
    :result
    (gen/for-all*
      [(reference-keys-gen) (s/gen ::kss-specs/docs)]
      (fn [ref-keys docs]
        (map (fn [doc ref-key]
               (assoc doc
                      :reference (clojure.string/join "." ref-key)
                      :reference-key ref-key))
             docs
             ref-keys)))))

(s/def ::styleguide ::sections)





;; private api

(s/fdef protocol55.styleguide.alpha/weighted-reference-key
        :args (s/cat :weight-map ::weight-map :ref-key ::reference-key)
        :ret ::weighted-reference-key)

(s/def ::weighted-reference-key-compare-args
  (s/cat :keys (s/with-gen (s/cat :a ::weighted-reference-key
                                  :b ::weighted-reference-key)
                 weighted-reference-keys-pair-gen)))

(s/fdef protocol55.styleguide.alpha/weighted-reference-key-compare
        :args ::weighted-reference-key-compare-args
        :ret number?)

(s/def ::reference-key-docs
  (s/with-gen (s/coll-of (s/merge ::kss-specs/kss (s/keys :req-un [::reference-key])))
    strict-docs-gen))

(s/fdef protocol55.styleguide.alpha/build-weight-map
        :args (s/cat :docs (s/nilable ::reference-key-docs))
        :ret ::weight-map)

(s/def ::base-ref-depth-args (s/cat :k1 (s/nilable ::reference-key) :k2 (s/nilable ::reference-key)))

(s/fdef protocol55.styleguide.alpha/base-ref-depth
        :args ::base-ref-depth-args
        :ret int?)

(s/fdef protocol55.styleguide.alpha/assign-section-ids
        :args (s/cat :docs ::reference-key-docs)
        :ret ::sections)





;; public api

(s/fdef protocol55.styleguide.alpha/reference-key
        :args (s/cat :reference ::reference)
        :ret ::reference-key)

(s/fdef protocol55.styleguide.alpha/styleguide
        :args (s/cat :docs (s/? (s/nilable (s/with-gen ::kss-specs/docs strict-docs-gen))))
        :ret (s/nilable ::styleguide))

(s/def ::sections-seq-args
  (s/alt :arity-1 (s/cat :sg ::styleguide)
         :arity-2 (s/cat :ref-key ::reference-key :sg ::styleguide)
         :arity-3 (s/cat :ref-key ::reference-key :max-depth int? :sg ::styleguide)
         :arity-4 (s/cat :ref-key ::reference-key :min-depth int? :max-depth int? :sg ::styleguide)))

(s/fdef protocol55.styleguide.alpha/sections-seq
        :args ::sections-seq-args
        :ret ::sections)





(comment
  (require '[clojure.spec.test.alpha :as st])

  (def check-opts {:clojure.spec.test.check/opts {:num-tests 50 :max-size 50}})
  (st/check `protocol55.styleguide.alpha/reference-key)
  (st/check `protocol55.styleguide.alpha/weighted-reference-key)
  (st/check `protocol55.styleguide.alpha/weighted-reference-key-compare)
  (st/check `protocol55.styleguide.alpha/base-ref-depth)
  (st/check `protocol55.styleguide.alpha/assign-section-ids check-opts)
  (st/check `protocol55.styleguide.alpha/build-weight-map check-opts)
  (st/check `protocol55.styleguide.alpha/styleguide)
  (st/check `protocol55.styleguide.alpha/sections-seq check-opts)

  (st/instrument)
  (st/unstrument)
  )
