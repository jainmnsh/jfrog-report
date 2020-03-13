'use strict';
const _ = require('lodash');
const Promise = require('bluebird');
const env = require('env-var');
const log = require('../../config/winston');
const service = require('./service');
const urlJoin = require('url-join');
const util = require('util');

const END_POINT = env('ARTIFACTORY_ENDPOINT', 'http://35.223.21.70/artifactory/api').asString();
const JFROG_STORAGE_API = env('JFROG_STORAGE_API', '/storage/').asString();
const JFROG_REPO = env('JFROG_STORAGE_API', 'jcenter-cache').asString();
const JFROG_SEARCH = env('JFROG_SEARCH', '/search/aql').asString();
const query_template = 'items.find({ "repo":{"$eq":"REPO"}})'


exports.getRepoData = () => {
  const input = {
    params: _.replace(query_template, 'REPO', JFROG_REPO),
    url: urlJoin(END_POINT, JFROG_SEARCH),
    method: 'post'
  };
  return Promise.props({
    getQueryResults: getQueryResults(input)
  });
};

 const getTotalCount = (artifact) => {
  const params = {
    url: urlJoin(END_POINT, JFROG_STORAGE_API, artifact.repo, artifact.path, artifact.name, '?stats'),
    method: 'get'
  };
  return service(params)
    .tap((results) => log.debug(util.format(' function getTotalCount: %o', results)));
};


/**
 * Returns an object containing the different types of elevator equipment
 * @return {Promise}
 */
function getQueryResults(params) {
  log.info('getQueryResults', params);
  return service(params)
    .then((queryResults) => {
      return _.filter(queryResults.results, function (record) {
        return _.includes(record.name, '.jar');
      });
    })
    .then((filterResults) => {
      return Promise.map(filterResults, (record) => {
        return getTotalCount(record);
      });
    })
    .then((results) => {
      return  _.orderBy(results, ['downloadCount'],  ['desc']);
    })
    .tap((sortresults) => log.debug( util.format('Sorted Results %o', sortresults)))
}

