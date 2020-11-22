package com.github.thibaultbee.srtdroid.enums

enum class ErrorType {
    EUNKNOWN,
    SUCCESS,

    ECONNSETUP,
    ENOSERVER,
    ECONNREJ,
    ESOCKFAIL,
    ESECFAIL,
    ESCLOSED,

    ECONNFAIL,
    ECONNLOST,
    ENOCONN,

    ERESOURCE,
    ETHREAD,
    ENOBUF,
    ESYSOBJ,

    EFILE,
    EINVRDOFF,
    ERDPERM,
    EINVWROFF,
    EWRPERM,

    EINVOP,
    EBOUNDSOCK,
    ECONNSOCK,
    EINVPARAM,
    EINVSOCK,
    EUNBOUNDSOCK,
    ENOLISTEN,
    ERDVNOSERV,
    ERDVUNBOUND,
    EINVALMSGAPI,
    EINVALBUFFERAPI,
    EDUPLISTEN,
    ELARGEMSG,
    EINVPOLLID,
    EPOLLEMPTY,

    EASYNCFAIL,
    EASYNCSND,
    EASYNCRCV,
    ETIMEOUT,
    ECONGEST,

    EPEERERR;

    external override fun toString(): String
}