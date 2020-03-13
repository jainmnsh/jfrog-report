/**
 * Created by mjain on 3/12/20.
 */
'use strict';
const request = require('request-promise');
const log = require('../../config/winston');
const VError = require('verror');
const env = require('env-var');
const _ = require('lodash');
const USERNAME = env('USERNAME', 'admin').asString();
const PASSWORD = env('PASSWORD', 'W11BaUPQDk').asString();


module.exports = function callService(params) {
  const url = params.url;
  const auth = "Basic " + new Buffer(USERNAME + ":" + PASSWORD).toString("base64");

  const options = {
    url: url,
    headers: params.headers || {"Authorization": auth},
    method: params.method,
    resolveWithFullResponse: true,
    gzip: true,
    transform: function (body, response) {
      if (_.includes(response.headers['content-type'], 'json')) {
        response.body = JSON.parse(body);
      }
      return response;
    }
  };

  if (params.method.toLowerCase() === 'get') {
    options.qs = params.params;
  } else {
    options.body = params.params;
  }

  return request(options)
    .then((response) => {
      return response.body;
    })
    .catch((e) => {
      const error = new VError(e, 'Error making the api Call');
      log.error(error);
    });
};


