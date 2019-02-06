(ns fiddle.core
  (:require [clojure.test :as t]
            [clojure.java.jdbc :as jdbc]
            [clojure.spec.alpha :as s]
            [clojure.pprint :as pprint :refer [pp]]
            [clojure.string :as string :refer [join]]))
            [clojure.edn :refer [read-string]]))

(def db-spec (-> "credentials.edn"
                 slurp
                 read-string))

(jdbc/query db-spec ["select count(*) from information_schema.tables"])

(s/def ::from
  (s/cat :clause (partial = :from)
         :expression (s/or :tablename keyword?)
         :expression (s/or :tablename keyword?)))

(s/def ::column-expression
  (s/or :wildcard (partial = :*)
        :columns (s/and
                   vector?
                   (s/coll-of keyword?)))
  keyword?)

(s/def ::select
  (s/cat :clause (partial = :select)
         :column-expression-list (s/and
                                   vector?
                                   (s/+ ::column-expression))
         :from (s/spec ::from)
         :rest (s/* (complement nil?))))


(def query '(:select [:titre]
                     (:from :contenu)))

(pprint/pprint (s/explain-data ::select query))
(pprint/pprint (s/conform ::select query))
(pp)

(defmulti colums first)
(defmethod colums first)

(defmulti from first)
(defmethod from
  [])

(defmulti select :clause)
(defmethod select :select
  [clause]
  ["select" 
   (columns (:columns))
   (from (:from clause))])
