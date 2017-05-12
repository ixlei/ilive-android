'use script';

const mysql = require('mysql');

const config = require('../config.js');

const mysqlConfig = config.mysql;
const { host, user, password, database } = mysqlConfig;

module.exports = mysql.createPool({
    connectionLimit: 20,
    host,
    user,
    password,
    database
});