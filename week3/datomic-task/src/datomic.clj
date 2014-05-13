(ns datomic
  (:require
    [datomic.api :as d]
    [clojure.edn :as edn]
    [clojure.java.io :as io]))

(def db-url "datomic:free://localhost:4334/imdb")

(d/create-database db-url)
(def conn (d/connect db-url))
(defn db [] (d/db conn))

;; hint: сделайте extenal id из feature :id
(def schema [
  { :db/ident       :series
    :db/id          (d/tempid :db.part/db)}

  { :db/ident       :episode
    :db/id          (d/tempid :db.part/db)}

  { :db/ident       :movie
    :db/id          (d/tempid :db.part/db)}

  { :db/ident       :video
    :db/id          (d/tempid :db.part/db)}

  { :db/ident       :tv-movie
    :db/id          (d/tempid :db.part/db)}

  { :db/ident       :videogame
    :db/id          (d/tempid :db.part/db)}

  { :db/ident       :feature/id
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/id          (d/tempid :db.part/db)
    :db.install/_attribute :db.part/db }

  { :db/ident       :feature/type
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/id          (d/tempid :db.part/db)
    :db.install/_attribute :db.part/db }

  { :db/ident       :feature/title
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/id          (d/tempid :db.part/db)
    :db.install/_attribute :db.part/db }

  { :db/ident       :feature/year
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/id          (d/tempid :db.part/db)
    :db.install/_attribute :db.part/db }

  { :db/ident       :feature/endyear
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/id          (d/tempid :db.part/db)
    :db.install/_attribute :db.part/db }

  { :db/ident       :feature/series
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/id          (d/tempid :db.part/db)
    :db.install/_attribute :db.part/db }

  { :db/ident       :feature/season
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/id          (d/tempid :db.part/db)
    :db.install/_attribute :db.part/db }

  { :db/ident       :feature/episode
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/id          (d/tempid :db.part/db)
    :db.install/_attribute :db.part/db }])

(defn reset []
  (d/release conn)
  (d/delete-database db-url)
  (d/create-database db-url)
  (alter-var-root #'conn (constantly (d/connect db-url)))
  @(d/transact conn schema))

(reset)


(defn- remove-nil-values [m]
  (remove #(nil? (second %)) m))

;; Формат файла:
;; { :type  =>   :series | :episode | :movie | :video | :tv-movie | :videogame
;;   :id    =>   str,  unique feature id
;;   :title =>   str,  feature title. Optional for episodes
;;   :year  =>   long, year of release. For series, this is starting year of the series
;;   :endyear => long, only for series, optional. Ending year of the series. If omitted, series still airs
;;   :series  => str,  only for episode. Id of enclosing series
;;   :season  => long, only for episode, optional. Season number
;;   :episode => long, only for episode, optional. Episode number
;; }
;; hint: воспользуйтесь lookup refs чтобы ссылаться на features по внешнему :id
(defn import []
  (with-open [rdr (io/reader "features.2014.edn")]
    (doseq [line (line-seq rdr)
            :let [feature (edn/read-string line)
                  nil-entity { :db/id (d/tempid :db.part/user)
                               :feature/id (:id feature)
                               :feature/type (:type feature)
                               :feature/title (:title feature)
                               :feature/year (:year feature)
                               :feature/endyear (:endyear feature)
                               :feature/series
                                 (when-let [series (:series feature)] [:feature/id series])
                               :feature/season (:season feature)
                               :feature/episode (:episode feature) }
                  entity (into {} (remove-nil-values nil-entity))]]
        @(d/transact conn [entity]))))

(import)

(d/q '[:find ?id
       :where
         [?id :feature/type _]]
     (db))
;; Найти все пары entity указанных типов с совпадающими названиями
;; Например, фильм + игра с одинаковым title
;; Вернуть #{[id1 id2], ...}
;; hint: data patterns

(defn siblings [db type1 type2]
  (d/q '[:find ?id1 ?id2
         :where
           [?id1 :feature/title ?t]
           [?id1 :feature/type type1]
           [?id2 :feature/title ?t]
           [?id2 :feature/type type2]]
       db))

;;(siblings (db) :movie :videogame)

;; Найти сериал(ы) с самым ранним годом начала
;; Вернуть #{[id year], ...}
;; hint: aggregates

(defn oldest-series [db]
  (d/q '[:find ?id (min ?year)
         :where
           [?id :feature/year ?year]
           [?id :feature/type :series]]
       db))

;; Найти 3 сериала с наибольшим количеством серий в сезоне
;; Вернуть [[id season series-count], ...]
;; hint: aggregates, grouping

(defn longest-season [db]
  :TODO)

;; Найти 5 самых популярных названий (:title). Названия эпизодов не учитываются
;; Вернуть [[count title], ...]
;; hint: aggregation, grouping, predicates

(defn popular-titles [db]
  :TODO)
