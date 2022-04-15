(ns waddams.transaction-test
  (:require [clojure.test :as t]
            [waddams.transaction :as transaction])
  (:import (java.time LocalDateTime)))

(def now (LocalDateTime/now))

(def transactions
  [{:ts now
    :ticker "TSLA"
    :type :buy
    :amount 10
    :transaction-amount 10000
    :transaction-costs 10}
   {:ts now
    :ticker "NVDA"
    :type :buy
    :amount 10
    :transaction-amount 3000
    :transaction-costs 3}
   {:ts (.plusDays now 2)
    :ticker "TSLA"
    :type :sell
    :amount 12
    :transaction-amount 15000
    :transaction-costs 15}
   {:ts (.plusDays now 1)
    :ticker "TSLA"
    :type :buy
    :amount 5
    :transaction-amount 2500
    :transaction-costs 2.5}
   {:ts (.plusDays now 1)
    :ticker "NVDA"
    :type :sell
    :amount 10
    :transaction-amount 2900
    :transaction-costs 2.9}
   {:ts (.plusDays now 3)
    :ticker "TSLA"
    :type :buy
    :amount 5
    :transaction-amount 4000
    :transaction-costs 4}])

(def completed-grouped-transactions
  {"TSLA" [{:ts now
            :ticker "TSLA"
            :type :buy
            :amount 10
            :transaction-amount 10000
            :transaction-costs 10
            :stock-after-transaction 10
            :sellable-amount-left 0
            :unit-price (/ 10000 10)}
           {:ts (.plusDays now 1)
            :ticker "TSLA"
            :type :buy
            :amount 5
            :transaction-amount 2500
            :transaction-costs 2.5
            :stock-after-transaction 15
            :sellable-amount-left 3
            :unit-price (/ 2500 5)}
           {:ts (.plusDays now 2)
            :ticker "TSLA"
            :type :sell
            :amount 12
            :transaction-amount 15000
            :transaction-costs 15
            :stock-after-transaction 3
            :unit-price (/ 15000 12)
            :profit 4000}
           {:ts (.plusDays now 3)
            :ticker "TSLA"
            :type :buy
            :amount 5
            :transaction-amount 4000
            :transaction-costs 4
            :stock-after-transaction 8
            :sellable-amount-left 5
            :unit-price (/ 4000 5)}]
   "NVDA" [{:ts now
            :ticker "NVDA"
            :type :buy
            :amount 10
            :transaction-amount 3000
            :transaction-costs 3
            :stock-after-transaction 10
            :sellable-amount-left 0
            :unit-price (/ 3000 10)}
           {:ts (.plusDays now 1)
            :ticker "NVDA"
            :type :sell
            :amount 10
            :transaction-amount 2900
            :transaction-costs 2.9
            :stock-after-transaction 0
            :unit-price (/ 2900 10)
            :profit -100}]})

(t/deftest fifo-buy-test
  (t/is (= [{:ts now
             :type :buy
             :amount 10
             :transaction-amount 10000
             :transaction-costs 10
             :stock-after-transaction 10
             :sellable-amount-left 10
             :unit-price (/ 10000 10)}]
           (transaction/fifo-buy [] {:ts now
                                     :type :buy
                                     :amount 10
                                     :transaction-amount 10000
                                     :transaction-costs 10})))
  (t/is (= (-> completed-grouped-transactions
               (get "TSLA")
               (conj {:ts (.plusDays now 4)
                      :type :buy
                      :amount 2
                      :transaction-amount 3000
                      :transaction-costs 3
                      :stock-after-transaction 10
                      :sellable-amount-left 2
                      :unit-price (/ 3000 2)}))
           (transaction/fifo-buy (get completed-grouped-transactions "TSLA")
                                 {:ts (.plusDays now 4)
                                  :type :buy
                                  :amount 2
                                  :transaction-amount 3000
                                  :transaction-costs 3}))))

(t/deftest fifo-sell-test
  (t/is (= [{:ts now
             :type :buy
             :amount 5
             :transaction-amount 500
             :transaction-costs 5
             :stock-after-transaction 5
             :sellable-amount-left 0
             :unit-price (/ 500 5)}
            {:ts now
             :type :buy
             :amount 5
             :transaction-amount 1000
             :transaction-costs 10
             :stock-after-transaction 10
             :sellable-amount-left 2
             :unit-price (/ 1000 5)}
            {:ts (.plusDays now 1)
             :type :sell
             :amount 8
             :transaction-amount 1200
             :transaction-costs 12
             :stock-after-transaction 2
             :unit-price (/ 1200 8)
             :profit 100}]
           (transaction/fifo-sell [{:ts now
                                    :type :buy
                                    :amount 5
                                    :transaction-amount 500
                                    :transaction-costs 5
                                    :stock-after-transaction 5
                                    :sellable-amount-left 5
                                    :unit-price (/ 500 5)}
                                   {:ts now
                                    :type :buy
                                    :amount 5
                                    :transaction-amount 1000
                                    :transaction-costs 10
                                    :stock-after-transaction 10
                                    :sellable-amount-left 5
                                    :unit-price (/ 1000 5)}]
                                  {:ts (.plusDays now 1)
                                   :type :sell
                                   :amount 8
                                   :transaction-amount 1200
                                   :transaction-costs 12}))))

(t/deftest completed-ticker-transactions-test
  (t/is (= (get completed-grouped-transactions "TSLA")
           (->> transactions
                (sort-by :ts)
                (filter #(= (:ticker %) "TSLA"))
                (transaction/completed-ticker-transactions)))))

(t/deftest grouped-transactions-test
  (let [grouped-transactions (transaction/grouped-transactions transactions)]
    (t/is (= #{"TSLA" "NVDA"} (set (keys grouped-transactions))))
    (doseq [ticker ["TSLA" "NVDA"]]
      (t/is (= (filter #(= (:ticker %) ticker) transactions)
               (get grouped-transactions ticker))))))

(t/deftest completed-transactions-test
    (t/is (= completed-grouped-transactions
             (transaction/completed-grouped-transactions transactions))))
