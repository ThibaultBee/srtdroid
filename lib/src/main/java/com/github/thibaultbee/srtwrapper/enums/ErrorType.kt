package com.github.thibaultbee.srtwrapper.enums

enum class ErrorType {
    EUNKNOWN,
    SUCCESS,

    ECONNSETUP,
    ENOSERVER,
    ECONNREJ,
    ESOCKFAIL,
    ESECFAIL,

    ECONNFAIL,
    ECONNLOST,
    ENOCONN,

    ERESOURCE,
    ETHREAD,
    ENOBUF,

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

    EASYNCFAIL,
    EASYNCSND,
    EASYNCRCV,
    ETIMEOUT,
    ECONGEST,

    EPEERERR;

    private external fun nativeStrError(): String

    override fun toString(): String {
        return nativeStrError()
    }
}