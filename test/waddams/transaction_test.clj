(ns waddams.transaction-test
  (:require [clojure.test :as t]
            [waddams.transaction :as transaction])
  (:import (java.time LocalDateTime)))

(def now (LocalDateTime/now))

(def transactions
  [{:id "Some buy id"
    :time now
    :ticker "TSLA"
    :type :buy
    :amount 10
    :transaction-amount 10000
    :transaction-costs 10}
   {:id "Another buy id"
    :time now
    :ticker "NVDA"
    :type :buy
    :amount 10
    :transaction-amount 3000
    :transaction-costs 3}
   {:id "Some sell id"
    :time (.plusDays now 2)
    :ticker "TSLA"
    :type :sell
    :amount 12
    :transaction-amount 15000
    :transaction-costs 15}
   {:id "123"
    :time (.plusDays now 1)
    :ticker "TSLA"
    :type :buy
    :amount 5
    :transaction-amount 2500
    :transaction-costs 2.5}
   {:id "foo"
    :time (.plusDays now 1)
    :ticker "NVDA"
    :type :sell
    :amount 10
    :transaction-amount 2900
    :transaction-costs 2.9}
   {:id "BAR"
    :time (.plusDays now 3)
    :ticker "TSLA"
    :type :buy
    :amount 5
    :transaction-amount 4000
    :transaction-costs 4}
   {:id "Some fund buy"
    :time (.plusDays now 10)
    :ticker "NORDNET INDEKS USA EUR"
    :type :buy
    :amount 3.449
    :transaction-amount 500
    :transaction-costs 0}
   {:id "Some fund sell"
    :time (.plusDays now 20)
    :ticker "NORDNET INDEKS USA EUR"
    :type :sell
    :amount 3.449
    :transaction-amount 510
    :transaction-costs 0}])

(def completed-grouped-transactions
  {"TSLA" [{:id "Some buy id"
            :time now
            :ticker "TSLA"
            :type :buy
            :amount 10
            :transaction-amount 10000
            :transaction-costs 10
            :stock-after-transaction 10
            :sellable-amount-left 0
            :unit-price (/ 10000 10)}
           {:id "123"
            :time (.plusDays now 1)
            :ticker "TSLA"
            :type :buy
            :amount 5
            :transaction-amount 2500
            :transaction-costs 2.5
            :stock-after-transaction 15
            :sellable-amount-left 3
            :unit-price (/ 2500 5)}
           {:id "Some sell id"
            :time (.plusDays now 2)
            :ticker "TSLA"
            :type :sell
            :amount 12
            :transaction-amount 15000
            :transaction-costs 15
            :stock-after-transaction 3
            :unit-price (/ 15000 12)
            :profit 4000}
           {:id "BAR"
            :time (.plusDays now 3)
            :ticker "TSLA"
            :type :buy
            :amount 5
            :transaction-amount 4000
            :transaction-costs 4
            :stock-after-transaction 8
            :sellable-amount-left 5
            :unit-price (/ 4000 5)}]
   "NVDA" [{:id "Another buy id"
            :time now
            :ticker "NVDA"
            :type :buy
            :amount 10
            :transaction-amount 3000
            :transaction-costs 3
            :stock-after-transaction 10
            :sellable-amount-left 0
            :unit-price (/ 3000 10)}
           {:id "foo"
            :time (.plusDays now 1)
            :ticker "NVDA"
            :type :sell
            :amount 10
            :transaction-amount 2900
            :transaction-costs 2.9
            :stock-after-transaction 0
            :unit-price (/ 2900 10)
            :profit -100}]
   "NORDNET INDEKS USA EUR" [{:id "Some fund buy"
                              :time (.plusDays now 10)
                              :ticker "NORDNET INDEKS USA EUR"
                              :type :buy
                              :amount 3.449
                              :transaction-amount 500
                              :transaction-costs 0
                              :stock-after-transaction 3.449
                              :sellable-amount-left 0
                              :unit-price (/ 500 3.449)}
                             {:id "Some fund sell"
                              :time (.plusDays now 20)
                              :ticker "NORDNET INDEKS USA EUR"
                              :type :sell
                              :amount 3.449
                              :transaction-amount 510
                              :transaction-costs 0
                              :stock-after-transaction 0.0
                              :unit-price (/ 510 3.449)
                              :profit 10.000000000000068}]})

(def tickers (-> completed-grouped-transactions keys set))

(t/deftest fifo-buy-test
  (t/is (= [{:id "a"
             :time now
             :type :buy
             :amount 10
             :transaction-amount 10000
             :transaction-costs 10
             :stock-after-transaction 10
             :sellable-amount-left 10
             :unit-price (/ 10000 10)}
            {:id "b"
             :time (.plusDays now 1)
             :type :buy
             :amount 5
             :transaction-amount 10000
             :transaction-costs 10
             :stock-after-transaction 15
             :sellable-amount-left 5
             :unit-price (/ 10000 5)}]
           (transaction/fifo-buy [{:id "a"
                                   :time now
                                   :type :buy
                                   :amount 10
                                   :transaction-amount 10000
                                   :transaction-costs 10
                                   :stock-after-transaction 10
                                   :sellable-amount-left 10
                                   :unit-price (/ 10000 10)}]
                                 {:id "b"
                                  :time (.plusDays now 1)
                                  :type :buy
                                  :amount 5
                                  :transaction-amount 10000
                                  :transaction-costs 10}))))

(t/deftest fifo-sell-test
  (t/is (= [{:id "a"
             :time now
             :type :buy
             :amount 5
             :transaction-amount 500
             :transaction-costs 5
             :stock-after-transaction 5
             :sellable-amount-left 0
             :unit-price (/ 500 5)}
            {:id "b"
             :time now
             :type :buy
             :amount 5
             :transaction-amount 1000
             :transaction-costs 10
             :stock-after-transaction 10
             :sellable-amount-left 2
             :unit-price (/ 1000 5)}
            {:id "c"
             :time (.plusDays now 1)
             :type :sell
             :amount 8
             :transaction-amount 1200
             :transaction-costs 12
             :stock-after-transaction 2
             :unit-price (/ 1200 8)
             :profit 100}]
           (transaction/fifo-sell [{:id "a"
                                    :time now
                                    :type :buy
                                    :amount 5
                                    :transaction-amount 500
                                    :transaction-costs 5
                                    :stock-after-transaction 5
                                    :sellable-amount-left 5
                                    :unit-price (/ 500 5)}
                                   {:id "b"
                                    :time now
                                    :type :buy
                                    :amount 5
                                    :transaction-amount 1000
                                    :transaction-costs 10
                                    :stock-after-transaction 10
                                    :sellable-amount-left 5
                                    :unit-price (/ 1000 5)}]
                                  {:id "c"
                                   :time (.plusDays now 1)
                                   :type :sell
                                   :amount 8
                                   :transaction-amount 1200
                                   :transaction-costs 12}))))

(t/deftest completed-ticker-transactions-test
  (doseq [ticker tickers]
   (t/is (= (get completed-grouped-transactions ticker)
            (->> transactions
                 (sort-by :time)
                 (filter #(= (:ticker %) ticker))
                 (transaction/completed-ticker-transactions))))))

(t/deftest grouped-transactions-test
  (let [grouped-transactions (transaction/grouped-transactions transactions)]
    (t/is (= #{"TSLA" "NVDA" "NORDNET INDEKS USA EUR"} (set tickers)))
    (doseq [ticker tickers]
      (t/is (= (filter #(= (:ticker %) ticker) transactions)
               (get grouped-transactions ticker))))))

(t/deftest completed-transactions-test
  (t/is (= completed-grouped-transactions
           (transaction/completed-grouped-transactions transactions))))
