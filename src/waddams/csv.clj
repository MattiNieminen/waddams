(ns waddams.csv
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.time LocalDateTime)))

(defn csv-row->transaction [csv-line]
  (let [[id time ticker type amount transaction-amount transaction-costs]
        (str/split csv-line #",")
        type (-> type str/lower-case keyword)]
    {:id id
     :time (LocalDateTime/parse time)
     :ticker ticker
     :type (if (contains? #{:buy :sell} type)
             type
             (throw (ex-info "Unknown transaction type" {:type type})))
     :amount (bigdec amount)
     :transaction-amount (bigdec transaction-amount)
     :transaction-costs (bigdec transaction-costs)}))

(defn read-transactions [file-path]
  (let [rdr (-> file-path io/file io/reader)]
    {:close! #(.close rdr)
     :transactions (->> rdr
                        line-seq
                        rest
                        (map csv-row->transaction))}))

(def output-headers-row
  "Id,Time,Ticker,Type,Amount,Transaction amount,Transaction costs,Stock after transaction,Sellable amount left,Unit price,Profit/Loss")

(defn write-completed-transactions! [file-path completed-transactions]
  (io/delete-file file-path true)
  (with-open [w (io/writer file-path)]
    (.write w (str output-headers-row "\n"))
    (doseq [{:keys [id time ticker type amount transaction-amount
                    transaction-costs stock-after-transaction
                    sellable-amount-left unit-price profit]}
            completed-transactions]
      (.write w (format "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n"
                        id
                        time
                        ticker
                        (-> type name str/upper-case)
                        amount
                        transaction-amount
                        transaction-costs
                        stock-after-transaction
                        (if sellable-amount-left sellable-amount-left "")
                        unit-price
                        (if profit profit ""))))))
