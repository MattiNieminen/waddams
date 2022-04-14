# Waddams

Ex-cuse me, ye-I-I believe you have my stapler...

## Introduction

This application calculates profits and losses of trading assets and keeps
track of current inventory for accouting purposes. The author uses this
application for stocks, but the application can be applied for any assets and
transactions. The used inventory control method is FIFO.

### Disclaimer

**There are no guarantees that the calculations this application produces are
correct, so use at your own risk. None of the parties developing this project
accept any responsiblity or liability regarding the use of this software and its
related material.**

## Getting started

The prerequisites for running the application are:

* The CSV file of transactions.
* Clojure CLI tools are installed.

### Transaction as a CSV file

The application reads the transactions from a single CSV file. The first line
of the file must be used for headers. The headers in order are:

* **Local** date and time in
  [ISO 8601 format](https://en.wikipedia.org/wiki/ISO_8601)
* Asset ticker
* Transaction type, either buy or sell
* Amount of asset either bought or sold
* Transaction amount without costs
* Transaction costs

Below is a simple example for stocks:

```csv
Timestamp,Ticker,Type,Amount,Transaction amount,Transaction costs
2020-01-01T12:00:00,TSLA,BUY,10,10000,10
2020-01-01T12:00:00,NVDA,BUY,10,3000,3
2020-01-03T12:00:00,TSLA,SELL,12,15000,15
2020-01-02T12:00:00,TSLA,BUY,5,2500,2.5
2020-01-05T12:00:00,NVDA,SELL,10,2900,2.9
2020-01-06T12:00:00,TSLA,BUY,5,4000,4
```

This application calculates profits and losses after every sell and keeps track
of inventory after every transaction. The application does not care whether or
not the tickers are real. Since the author uses this application for business
accounting, transaction costs are only for reporting purposes and are not used
for calculating profits.

The application sorts the transactions using the timestamp column.

### Running the application

The application can be run with the following command:

```bash
clj -M -m waddams.core [path-to-the-csv-file-from-project-root]
```

The application will then write a CSV file for each ticker. The CSV files
is similar to the input file with a couple of columns added:

* `Stock after transaction`: the amount of asset owned after the transaction
* `Sellable amount left`: filled only for buys. The amount of asset from the
  buy that hasn't been sold yet.
* `Unit price`: the asset price of a single unit
* `Profit/Loss`: filled only for sales. The profit or loss made from the
    selling transaction

The application will also write a file `all-transactions.csv` for convenience.
This file is handy for importing transactions to accouting software after.

### Limitations

* The application does not check if there's enough stock to sell the assets
  which means that the stock can be negative at times. It's up to the user
  to verify that the input CSV file is correct.

## Development

Installation of Clojure CLI Tools is necessary to develop this project. After
installing, REPL from command line or from an IDE can be used for the
development.
