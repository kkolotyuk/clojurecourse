(ns test-datomic
  (:use
    datomic
    clojure.test))

(deftest test-siblings
  (is (= (siblings (db) :movie :videogame)
         #{["Destiny (2014/I)"    "Destiny (2014) (VG)"]
           ["Destiny (2014/II)"   "Destiny (2014) (VG)"]
           ["Destiny (2014/III)"  "Destiny (2014) (VG)"]
           ["The Amazing Spider-Man 2 (2014)" "The Amazing Spider-Man 2 (2014) (VG)"]
           ["Clandestine (2014)"  "Clandestine (2014) (VG)"]}))
  (is (= (siblings (db) :series :videogame)
         #{["\"Destiny\" (2013)"          "Destiny (2014) (VG)"]
           ["\"Game of Thrones\" (2011)"  "Game of Thrones (2014) (VG)"]
           ["\"Sonic Boom\" (2014)"       "Sonic Boom (2014) (VG)"]})))

(deftest test-oldest-series
  (is (= (oldest-series (db))
         #{["\"Today\" (1952)" 1952]})))

(deftest test-longest-season
  (is (= (longest-season (db))
         [["\"Familie\" (1991)"           1 88]
          ["\"Let's Ask America\" (2012)" 2 87]
          ["\"Belmonte\" (2013)"          1 78]])))

(deftest test-popular-titles
  (is (= (popular-titles (db))
         [[9 "Alone"]
          [8 "Home"]
          [6 "Retribution"]
          [6 "Destiny"]
          [6 "Broken"]])))
