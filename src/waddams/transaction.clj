(ns waddams.transaction)

(defn fifo-buy [completed-ticker-transactions
                {:keys [amount transaction-amount] :as transaction}]
  (conj completed-ticker-transactions
        (-> transaction
            (assoc :stock-after-transaction (-> completed-ticker-transactions
                                                last
                                                :stock-after-transaction
                                                (or 0)
                                                (+ amount)))
            (assoc :sellable-amount-left amount)
            (assoc :unit-price (/ transaction-amount amount)))))

(defn fifo-sell* [completed-ticker-transactions
                      {:keys [amount transaction-amount]}]
  (let [sell-unit-price (/ transaction-amount amount)]
    (reduce (fn [{:keys [left-to-sell] :as acc}
                 {:keys [type sellable-amount-left unit-price]
                  :as completed-transaction}]
              (if (= type :buy)
                (let [sell-amount (min left-to-sell sellable-amount-left)]
                  (-> acc
                      (update :completed-ticker-transactions
                              conj
                              (assoc completed-transaction
                                     :sellable-amount-left
                                     (if (= sell-amount sellable-amount-left)
                                       0
                                       (- sellable-amount-left sell-amount))))
                      (assoc :left-to-sell (if (= sell-amount left-to-sell)
                                             0
                                             (- left-to-sell sell-amount)))
                      (update :profit + (* (- sell-unit-price
                                              unit-price)
                                           sell-amount))))
                (update acc
                        :completed-ticker-transactions
                        conj
                        completed-transaction)))
            {:completed-ticker-transactions []
             :left-to-sell amount
             :profit 0}
            completed-ticker-transactions)))

(defn fifo-sell [completed-ticker-transactions
                 {:keys [amount transaction-amount] :as transaction}]
  (let [{:keys [completed-ticker-transactions profit]}
        (fifo-sell* completed-ticker-transactions transaction)]
    (conj completed-ticker-transactions
          (-> transaction
              (assoc :stock-after-transaction (-> completed-ticker-transactions
                                                  last
                                                  :stock-after-transaction
                                                  (- amount)))
              (assoc :unit-price (/ transaction-amount amount))
              (assoc :profit profit)))))

(defn completed-ticker-transactions [ticker-transactions]
  (reduce (fn [completed-ticker-transactions {:keys [type] :as transaction}]
            (if (= type :buy)
              (fifo-buy completed-ticker-transactions transaction)
              (fifo-sell completed-ticker-transactions transaction)))
          []
          ticker-transactions))

(defn grouped-transactions [transactions]
  (reduce (fn [grouped-transactions {:keys [ticker] :as transaction}]
            (update grouped-transactions
                    ticker
                    #(conj (or %1 []) %2)
                    transaction))
          {}
          transactions))

(defn completed-grouped-transactions [transactions]
  (->> transactions
       (sort-by :time)
       grouped-transactions
       (reduce-kv (fn [completed-transactions ticker ticker-transactions]
                    (assoc completed-transactions
                           ticker
                           (completed-ticker-transactions ticker-transactions)))
                  {})))
