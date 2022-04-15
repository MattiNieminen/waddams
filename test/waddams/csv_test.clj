(ns waddams.csv-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as t]
            [waddams.csv :as csv])
  (:import (java.time LocalDateTime)))

(t/deftest csv-row->transaction-test
  (let [ts-str "2020-01-01T12:00:00"]
    (t/is (= {:id "Some buy id"
              :time (LocalDateTime/parse ts-str)
              :ticker "TSLA"
              :type :buy
              :amount 10M
              :transaction-amount 10000M
              :transaction-costs 10M}
             (csv/csv-row->transaction
              (str "Some buy id," ts-str ",TSLA,BUY,10,10000,10"))))))

(t/deftest read-transactions-test
  (let [{:keys [close! transactions]} (csv/read-transactions
                                       "resources/example.csv")]
    (t/is (= 8 (count transactions)))
    (t/is (= "Some buy id" (-> transactions first :id)))
    (t/is (= (LocalDateTime/parse "2020-01-01T12:00:00")
             (-> transactions second :time)))
    (t/is (= "NORDNET INDEKS USA EUR" (-> transactions last :ticker)))
    (close!)))

(t/deftest write-completed-transactions!-test
  (let [file-path "test-output.tmp.csv"
        _ (io/file file-path)
        completed-transactions [{:id "a"
                                 :time (LocalDateTime/parse
                                        "2020-01-01T12:00:00")
                                 :type :buy
                                 :ticker "NVDA"
                                 :amount 10
                                 :transaction-amount 3000
                                 :transaction-costs 3
                                 :stock-after-transaction 10
                                 :sellable-amount-left 0
                                 :unit-price (/ 3000 10)}
                                {:id "b"
                                 :time (LocalDateTime/parse
                                        "2021-01-01T12:00:00")
                                 :ticker "NVDA"
                                 :type :sell
                                 :amount 10
                                 :transaction-amount 2900
                                 :transaction-costs 2.9
                                 :stock-after-transaction 0
                                 :unit-price (/ 2900 10)
                                 :profit -100}]
        _ (csv/write-completed-transactions! file-path completed-transactions)
        rows (-> file-path slurp (str/split #"\n"))]
    (t/is (= csv/output-headers-row (first rows)))
    (t/is (= "a,2020-01-01T12:00,NVDA,BUY,10,3000,3,10,0,300," (second rows)))
    (t/is (= "b,2021-01-01T12:00,NVDA,SELL,10,2900,2.9,0,,290,-100" (nth rows 2)))
    (io/delete-file file-path true)))
