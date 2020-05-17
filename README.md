# Logging stage for hitachi Content Platform

The looging stage was build for HCI 1.6 and send logs to Fluentd or Fluent bit. 
The stage allows to collect log events on at the document level, allowing big control on success or error handling management. 
Ideally [fluentbit](https://fluentbit.io/) or [Fluentd](https://www.fluentd.org/) should be installed locally on a server on the same local network for maximum throughput. 

This was originally created to send control messages to [Elasticsearch](https://www.elastic.co/) and/or [Splunk](https://www.splunk.com/), as to control the documents that need to be reprocessed, because they were not processed successfully. 

## Features

- High Performance
- Data Parsing and filtering
- Reliability and Data Integrity handling backpressure and capable of data buffering 
- Security: built-in TLS/SSL support
- Extensibility
- Monitoring and stream processing 
- Portability

## Documentation

Our official project documentation is in progress. Please reach out to the author for more details. 

## Quick Start

### Build from Scratch

```bash
mvn initialize 
mvn clean package 
```

## Contacts 
If you are interested into more details, please send email to [Meanify](mail://meanify@gmail.com)

## License
Please refer to the file LICENCE.md