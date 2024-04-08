/*
 * Copyright (C) 2021 Thibault B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.thibaultbee.srtdroid.enums

import io.github.thibaultbee.srtdroid.models.Socket

/**
 * Parameter or returned value of [Socket.setSockFlag] and [Socket.getSockFlag].
 *
 * **See Also:** [API Socket Options](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md)
 */
enum class SockOpt {
    /**
     * The Maximum Transfer Unit
     */
    MSS,

    /**
     * If sending is blocking
     */
    SNDSYN,

    /**
     * If receiving is blocking
     */
    RCVSYN,

    /**
     * Initial Sequence Number (valid only after srt_connect or srt_accept-ed sockets)
     */
    ISN,

    /**
     * Flight flag size (window size)
     */
    FC,

    /**
     * Maximum buffer in sending queue
     */
    SNDBUF,

    /**
     * UDT receiving buffer size
     */
    RCVBUF,

    /**
     * Waiting for unsent data when closing
     */
    LINGER,

    /**
     * UDP sending buffer size
     */
    UDP_SNDBUF,

    /**
     * UDP receiving buffer size
     */
    UDP_RCVBUF,

    /**
     * Rendezvous connection mode
     */
    RENDEZVOUS,

    /**
     * [Socket.send] timeout
     */
    SNDTIMEO,

    /**
     * [Socket.recv] timeout
     */
    RCVTIMEO,

    /**
     * Reuse an existing port or create a new one
     */
    REUSEADDR,

    /**
     * Maximum bandwidth (bytes per second) that the connection can use
     */
    MAXBW,

    /**
     * Current socket state, see UDTSTATUS, read only
     */
    STATE,

    /**
     * Current available events associated with the socket
     */
    EVENT,

    /**
     * Size of data in the sending buffer
     */
    SNDDATA,

    /**
     * Size of data available for recv
     */
    RCVDATA,

    /**
     * Sender mode (independent of conn mode), for encryption, tsbpd handshake.
     */
    SENDER,

    /**
     * Enable/Disable TsbPd. Enable -> Tx set origin timestamp, Rx deliver packet at origin time + delay
     */
    TSBPDMODE,

    /**
     * NOT RECOMMENDED. SET: to both RCVLATENCY and PEERLATENCY. GET: same as RCVLATENCY.
     */
    LATENCY,

    /**
     * Estimated input stream rate.
     */
    INPUTBW,

    /**
     * Minimum estimate of input stream rate.
     */
    MININPUTBW,

    /**
     * MaxBW ceiling based on % over input stream rate. Applies when UDT_MAXBW=0 (auto).
     */
    OHEADBW,

    /**
     * Crypto PBKDF2 Passphrase size (0,10..64) 0:disable crypto
     */
    PASSPHRASE,

    /**
     * Crypto key len in bytes {16,24,32} Default: 16 (128-bit)
     */
    PBKEYLEN,

    /**
     * Key Material exchange status (UDT_SRTKmState)
     */
    KMSTATE,

    /**
     * IP Time To Live (passthru for system sockopt IPPROTO_IP/IP_TTL)
     */
    IPTTL,

    /**
     * IP Type of Service (passthru for system sockopt IPPROTO_IP/IP_TOS)
     */
    IPTOS,

    /**
     * Enable receiver pkt drop
     */
    TLPKTDROP,

    /**
     * Extra delay towards latency for sender TLPKTDROP decision (-1 to off)
     */
    SNDDROPDELAY,

    /**
     * Enable receiver to send periodic NAK reports
     */
    NAKREPORT,

    /**
     * Local SRT Version
     */
    VERSION,

    /**
     * Peer SRT Version (from SRT Handshake)
     */
    PEERVERSION,

    /**
     * Connect timeout in msec. Caller default: 3000, rendezvous (x 10)
     */
    CONNTIMEO,

    /**
     * Enable or disable drift tracer
     */
    DRIFTTRACER,

    /**
     * (GET) the current state of the encryption at the peer side
     */
    SNDKMSTATE,

    /**
     * (GET) the current state of the encryption at the agent side
     */
    RCVKMSTATE,

    /**
     * Maximum possible packet reorder tolerance (number of packets to receive after loss to send lossreport)
     */
    LOSSMAXTTL,

    /**
     * TsbPd receiver delay (mSec) to absorb burst of missed packet retransmission
     */
    RCVLATENCY,

    /**
     * Minimum value of the TsbPd receiver delay (mSec) for the opposite side (peer)
     */
    PEERLATENCY,

    /**
     * Minimum SRT version needed for the peer (peers with less version will get connection reject)
     */
    MINVERSION,

    /**
     * A string set to a socket and passed to the listener's accepted socket
     */
    STREAMID,

    /**
     * Congestion controller type selection
     */
    CONGESTION,

    /**
     * In File mode, use message API (portions of data with boundaries)
     */
    MESSAGEAPI,

    /**
     * Maximum payload size sent in one UDP packet (0 if unlimited)
     */
    PAYLOADSIZE,

    /**
     * Transmission type (set of options required for given transmission type)
     */
    TRANSTYPE,

    /**
     * After sending how many packets the encryption key should be flipped to the new key
     */
    KMREFRESHRATE,

    /**
     * How many packets before key flip the new key is annnounced and after key flip the old one decommissioned
     */
    KMPREANNOUNCE,

    /**
     * Connection to be rejected or quickly broken when one side encryption set or bad password
     */
    ENFORCEDENCRYPTION,

    /**
     * IPV6_V6ONLY mode
     */
    IPV6ONLY,

    /**
     * Peer-idle timeout (max time of silence heard from peer) in ms
     */
    PEERIDLETIMEO,

    /**
     *  Forward the [BINDTODEVICE] option on socket (pass packets only from that device)
     */
    BINDTODEVICE,

    /**
     * Add and configure a packet filter
     */
    PACKETFILTER,

    /**
     *  An option to select packet retransmission algorithm
     */
    RETRANSMITALGO
}