'use script';

const express = require('express');
const Router = express.Router();

const live = require('../models/live.js');

Router.post('/push_auth', (req, res, next) => {
    console.log(JSON.stringify(req.body));
    res.sendStatus(200);
});

Router.post('/publish', (req, res, next) => {
    const { code } = req.body;
    console.log(code);
    live.startLive(code)
        .then((data) => {
            if (data.ret == 0) {
                res.json({
                    ret: 0,
                    results: data.result
                });
            } else {
                res.json({
                    ret: 1,
                    results: data.result
                });
            }
        }).catch((err) => {
            res.json({
                ret: 2,
                results: err
            });
        })
});

Router.post('/play', (req, res, next) => {
    res.sendStatus(200);
});

Router.post('/playdone', (req, res, next) => {
    res.sendStatus(200);
});

Router.post('/publishdone', (req, res, next) => {
    res.sendStatus(200);
})

module.exports = Router;