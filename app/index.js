const express = require('express');
const app = express();
const probe = require('kube-probe');
const morgan = require('morgan');

const logger = require('./config/winston');

app.use(morgan("combined",{stream: logger.stream}));
probe(app);

app.use(require('cors')());

app.use('/reports', require('./lib/reports'));

var port = process.env.PORT || process.env.NODEJS_PORT || 8080;
var host = process.env.NODEJS_IP || '0.0.0.0';
app.listen(port, host, function () {
  logger.info('App started at: ' + new Date() + ' on port: ' + port);
});

