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
    console.log(JSON.stringify(req.body));
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
    const { code } = req.body;
    console.log(JSON.stringify(req.body));
    live.updateNum(0, code)
        .then((data) => {
            if (data && data.ret == 0) {
                res.json({
                    ret: 0,
                    results: 'success'
                });
                return;
            }
            res.json({ ret: 1, results: data.results });
        })
        .catch((err) => {
            res.json({ ret: 2, results: err });
        });
});

Router.post('/playdone', (req, res, next) => {
    const { code } = req.body;
    live.updateNum(1, code)
        .then((data) => {
            if (data && data.ret == 0) {
                res.json({
                    ret: 0,
                    results: 'success'
                });
                return;
            }
            res.json({ ret: 1, results: data.results });
        })
        .catch((err) => {
            res.json({ ret: 2, results: err });
        });
});

Router.post('/publishdone', (req, res, next) => {
    const { code } = req.body;
    live.liveEnd(code)
        .then((data) => {
            if (data && data.ret == 0) {
                res.json({ ret: 0, results: 'success' });
                return;
            }
            res.json({ ret: 1, results: 'error' });
        })
        .catch((err) => {
            res.json({ ret: 2, results: err.results });
        })
});

Router.get('/hotlive/:index', (req, res, next) => {
    const index = req.params.index || 0;
    const pageNumber = 20;
    live.getHotLive(index, pageNumber)
        .then((data) => {
            if (data && data.ret == 0) {
                res.json({ ret: 0, results: data.results });
                return;
            }
            res.json({ ret: 1, results: data.results })
        })
        .catch((err) => {
            res.json({ res: 2, results: err.results });
        })
});

module.exports = Router;