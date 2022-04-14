# Waddams

Ex-cuse me, ye-I-I believe you have my stapler...

## Introduction

This application calculates profits and losses of trading assets and keeps
track of current inventory for accouting purposes. The author uses this
application for stocks, but the application can be applied for any assets and
transactions. The used inventory control method is FIFO.

There's also a helper for writing transactions to Briox using browser
automation.

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

* Id of the transaction. Can be any string.
* **Local** date and time in
  [ISO 8601 format](https://en.wikipedia.org/wiki/ISO_8601).
* Asset ticker.
* Transaction type, either buy or sell.
* Amount of asset either bought or sold.
* Transaction amount without costs.
* Transaction costs.

Below is a simple example for stocks and funds:

```csv
Id,Time,Ticker,Type,Amount,Transaction amount,Transaction costs
Some buy id,2020-01-01T12:00:00,TSLA,BUY,10,10000,10
Another buy id,2020-01-01T12:00:00,NVDA,BUY,10,3000,3
Some sell id,2020-01-03T12:00:00,TSLA,SELL,12,15000,15
123,2020-01-02T12:00:00,TSLA,BUY,5,2500,2.5
foo,2020-01-05T12:00:00,NVDA,SELL,10,2900,2.9
BAR,2020-01-06T12:00:00,TSLA,BUY,5,4000,4
Some fund buy,2020-02-01T12:00:00,NORDNET INDEKS USA EUR,BUY,3.449,500,0
Some fund sell,2020-03-01T12:00:00,NORDNET INDEKS USA EUR,SELL,3.449,510,0
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
is similar to the input file with a couple of added columns:

* `Stock after transaction`: the amount of asset owned after the transaction
* `Sellable amount left`: filled only for buys. The amount of asset from the
  buy that hasn't been sold yet.
* `Unit price`: the asset price of a single unit
* `Profit/Loss`: filled only for sales. The profit or loss made from the
    selling transaction

The application will also write a file `all-transactions.csv` for convenience.
This file is handy for importing transactions to accouting software after.

### Briox utility

There's a separate application for writing transactions to Briox with browser
automation using Selenium and Chrome WebDriver. This application reads the
transactions from the same input file as the core application. To use the Briox
utility, run the following command:

```bash
clj -M -m waddams.briox [path-to-the-csv-file-from-project-root]
```

The application will open the browser and opens the Briox login page. Then, the
user has 30 seconds to login. After logging in, the application continues to
import the transactions to Briox. Finally the application closes the browser.

The used account numbers and other configuration is hard-coded, so code changes
should be made in order to use non-standard account numbers.

### Limitations

* The application does not check if there's enough stock to sell the assets
  which means that the stock can be negative at times. It's up to the user
  to verify that the input CSV file is correct.

## Development

Installation of Clojure CLI Tools is necessary to develop this project. After
installing, REPL from command line or from an IDE can be used for the
development.
