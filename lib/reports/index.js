'use strict';
const report = module.exports = require('express').Router();
report.use(require('body-parser').json());
const logger =  require('../../config/winston');
const artifact = require('./artifact');
const _ = require('lodash');

report.get('/artifacts/:count', async function (req, res) {
  const artifacts = await artifact.getRepoData();
 res.json({
   results: _.take(artifacts.getQueryResults, req.params.count)
  });
});



