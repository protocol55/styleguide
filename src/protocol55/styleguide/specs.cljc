(ns protocol55.styleguide.specs
  (:require [clojure.spec.alpha :as s]
            [spell-spec.alpha :as spell]))

;; main config

(s/def ::ext (s/and string? #(not (clojure.string/starts-with? % "."))))
(s/def ::mask (s/coll-of ::ext))
(s/def ::source string?)
(s/def ::root (s/and string? #(clojure.string/starts-with? % "/")))
(s/def ::css (s/coll-of string?))
(s/def ::theme-css (s/coll-of string?))
(s/def ::build-dir string?)
(s/def ::config (spell/keys :opt-un [::source ::mask ::root ::css ::theme-css ::build-dir]))
