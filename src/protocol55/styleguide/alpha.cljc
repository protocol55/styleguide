(ns protocol55.styleguide.alpha
  (:require [clojure.string :as s]))

(defn- parse-int [n]
  #?(:clj (Long/parseLong n))
  #?(:cljs (js/parseInt n 10)))

(def numeric-reference-regex #"^(\d+\.)*\d+$")

(defn numeric-reference? [x]
  (when (string? x)
    (boolean (re-find numeric-reference-regex x))))

(defn reference-key [reference]
  (cond
    (string? reference)
    (if (numeric-reference? reference)
      (->> (clojure.string/split reference #"\.")
           (reduce #(conj %1 (parse-int %2)) ())
           (drop-while zero?)
           reverse
           vec)
      (->> (clojure.string/split reference #"\.")
           (remove s/blank?)
           vec))

    (vector? reference)
    (->> reference reverse (drop-while zero?) reverse vec)

    :else
    reference))

(def reference-doc-pair (juxt (comp reference-key :reference) identity))

(defn weighted-reference-key-pad [k]
  (if (number? (peek (peek k))) 0 ""))

(defn weighted-reference-key*
  ([w a]
   [[(get-in w [1 a] 0.0) a]])
  ([w a b]
   [[(get-in w [1 a] 0.0) a]
    [(get-in w [2 b] 0.0) b]])
  ([w a b c]
   [[(get-in w [1 a] 0.0) a]
    [(get-in w [2 b] 0.0) b]
    [(get-in w [3 c] 0.0) c]])
  ([w a b c d]
   [[(get-in w [1 a] 0.0) a]
    [(get-in w [2 b] 0.0) b]
    [(get-in w [3 c] 0.0) c]
    [(get-in w [4 d] 0.0) d]])
  ([w a b c d e]
   [[(get-in w [1 a] 0.0) a]
    [(get-in w [2 b] 0.0) b]
    [(get-in w [3 c] 0.0) c]
    [(get-in w [4 d] 0.0) d]
    [(get-in w [5 e] 0.0) e]])
  ([w a b c d e & more]
    (into (weighted-reference-key* w a b c d e)
          (map-indexed (fn [i x] [(get-in w [(+ i 6) x] 0.0) x]) more))))

(defn weighted-reference-key [weight-map ref-key]
  (if (empty? ref-key)
    []
    (apply weighted-reference-key* weight-map ref-key)))

(defn weighted-reference-key-compare [a b]
  (let [alen (count a) blen (count b)]
    (if (= alen blen)
      (compare a b)
      (let [pad [0.0 (weighted-reference-key-pad (if (zero? blen) a b))]
            [a b] (if (> alen blen)
                    [a (into b (repeat (- alen blen) pad))]
                    [(into a (repeat (- blen alen) pad)) b])]
        (compare a b)))))

(defn build-weight-map
  "Retuns a nested map of reference key depths to reference key part to weight."
  [docs]
  (reduce (fn [weight-map {:keys [weight reference]
                           ref-key :reference-key
                           :as doc}]
            (if weight
              (let [depth (count ref-key)]
                (assoc-in weight-map [depth (last ref-key)] weight))
              weight-map))
          {}
          docs))

(defn base-ref-depth [prev-ref target-ref]
  (if (and prev-ref target-ref)
    (let [target-len (count target-ref)]
      (loop [idx 0 target-ref target-ref prev-ref prev-ref]
        (if (or (>= idx target-len)
                (not= (first target-ref) (first prev-ref)))
          (inc idx)
          (recur (inc idx)
                 (rest target-ref)
                 (rest prev-ref)))))
    1))

(defn assign-section-ids
  "Takes a coll of reference key to kss doc pairs and returns a coll of sections."
  [docs]
  (->> {:remaining docs}
       (iterate
         (fn [{[{:keys [reference-key] :as doc} & remaining] :remaining
               :keys [id prev-reference-key]
               :or {id []}
               :as state}]
           (when doc
             (let [base-ref-depth (base-ref-depth prev-reference-key reference-key)
                   id-depth (count id)
                   id (cond
                        (> base-ref-depth id-depth)
                        (into id (repeat (- base-ref-depth id-depth) 0))

                        (< base-ref-depth id-depth)
                        (vec (take base-ref-depth id))

                        :else id)
                   id (update id (dec base-ref-depth) (fnil inc 0))]
               (assoc state
                      :id id
                      :prev-reference-key reference-key
                      :yield (assoc doc :section-id (s/join "." id))
                      :remaining remaining)))))
       (take-while some?)
       (keep :yield)))

(def max-depth-default
  #?(:cljs js/Number.MAX_VALUE)
  #?(:clj Integer/MAX_VALUE))

(def referenceable-xf
  (filter (comp not s/blank? :reference)))

(def reference-keys-xf
  (map #(assoc % :reference-key (reference-key (:reference %)))))

(def styleguide-pair-xf
  (map #(vector (:reference-key %) %)))

(def prep-styleguide-docs-xf
  (comp referenceable-xf
        reference-keys-xf
        styleguide-pair-xf))

(defn prep-styleguide-docs
  [docs]
  (vals (into {} prep-styleguide-docs-xf docs)))

(defn weigh-reference-keys-fn
  [weight-map]
  #(assoc % :weighted-reference-key (weighted-reference-key weight-map (:reference-key %))))

(defn styleguide*
  [weight-map docs]
  (when (seq docs)
    (with-meta
      (->> (map (weigh-reference-keys-fn weight-map) docs)
           (sort-by :weighted-reference-key weighted-reference-key-compare)
           (assign-section-ids))
      {::weight-map weight-map})))

(defn styleguide
  "Returns an ordered sequence of sections of the kss documentation maps of docs with :reference keys."
  ([]
   (styleguide []))
  ([docs]
   (when-let [docs (prep-styleguide-docs docs)]
     (let [weight-map (build-weight-map docs)]
       (styleguide* weight-map docs)))))

(defn add
  "Adds the kss documentation maps of docs to styleguide, returning an ordered sequence of sections."
  [styleguide docs]
  (let [docs (prep-styleguide-docs (concat styleguide docs))
        weight-map (merge-with merge (::weight-map (meta styleguide)) (build-weight-map docs))]
    (styleguide* weight-map docs)))

(defn sections-seq
  "Returns an ordered sequence of sections of styleguide constrained by the base, min-depth and max-depth values.

  base - a vector of parts the reference must contain (default [])
  max-depth - the maximum depth of the references parts (default Integer/MAX_VALUE)
  min-depth - the minimum depth of the references parts (default 0)"
  ([styleguide]
   (sections-seq [] styleguide))
  ([base styleguide]
   (sections-seq base max-depth-default styleguide))
  ([base max-depth styleguide]
   (sections-seq base 0 max-depth styleguide))
  ([base min-depth max-depth styleguide]
   (let [depth (count base)]
     (filter
       (fn [{:keys [reference-key] :as doc}]
         (let [reference-key-depth (count reference-key)]
           (and (>= max-depth reference-key-depth min-depth)
                (= (take depth reference-key) base))))
       styleguide))))

(defn get-section-by-reference
  "Returns the section containing the :reference key reference or nil if not present."
  [styleguide reference]
  (let [ref-key (reference-key reference)
        depth (count ref-key)]
    (first (sections-seq ref-key depth depth styleguide))))
