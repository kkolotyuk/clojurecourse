(ns task02.db-test
  (:require [task02.db :refer :all]
            [task02.query :as q]
            [clojure.test :refer :all]))

(load-initial-data)

(deftest insert-test
  (load-initial-data)
  (testing "insertion..."
    (insert "student" {:id 10 :surname "Test" :year 2000})
    (let [rs (q/perform-query "select student where id = 10")]
      (is (not (empty? rs)))
      (is (= (count rs) 1))
      (is (= (:year (first rs)) 2000)))
    ))

(deftest delete-test
  (load-initial-data)
  (testing "deletion..."
    (delete "student" :where (q/make-where-function "id" "=" "1"))
    (let [rs (q/perform-query "select student where id = 1")]
      (is (empty? rs)))
    (is (= (count (q/perform-query "select student")) 2))
    ))

(deftest update-test
  (load-initial-data)
  (testing "update..."
    (let [rs (q/perform-query "select student where id = 1")]
      (is (not (empty? rs)))
      (is (= (count rs) 1))
      (is (= (:year (first rs)) 1998))
      )
    (update "student" {:year 2000} :where (q/make-where-function "id" "=" "1"))
    (let [rs (q/perform-query "select student where id = 1")]
      (is (not (empty? rs)))
      (is (= (count rs) 1))
      (is (= (:year (first rs)) 2000))
      )
    ))

(deftest create-table-test
  (load-initial-data)
  (testing "create-table..."
    (is (= (count @tables) 3))
    (create-table-from-csv "lecturer")
    (is (= (count @tables) 4))
    (let [rs (q/perform-query "select lecturer")]
      (is (not (empty? rs))))))

(deftest drop-table-test
  (load-initial-data)
  (testing "drop table..."
    (create-table-from-csv "lecturer")
    (is (= (count @tables) 4))
    (is (seq (get-table "lecturer")))
    (drop-table "lecturer")
    (is (= (count @tables) 3))
    (is (not (seq (get-table "lecturer"))))))
