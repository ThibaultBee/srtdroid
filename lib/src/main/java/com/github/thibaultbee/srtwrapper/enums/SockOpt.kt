package com.github.thibaultbee.srtwrapper.enums

enum class SockOpt {
    MSS,             // the Maximum Transfer Unit
    SNDSYN,          // if sending is blocking
    RCVSYN,          // if receiving is blocking
    ISN,             // Initial Sequence Number (valid only after srt_connect or srt_accept-ed sockets)
    FC,              // Flight flag size (window size)
    SNDBUF,          // maximum buffer in sending queue
    RCVBUF,          // UDT receiving buffer size
    LINGER,          // waiting for unsent data when closing
    UDP_SNDBUF,      // UDP sending buffer size
    UDP_RCVBUF,      // UDP receiving buffer size
    RENDEZVOUS,     // rendezvous connection mode
    SNDTIMEO,       // send() timeout
    RCVTIMEO,       // recv() timeout
    REUSEADDR,      // reuse an existing port or create a new one
    MAXBW,          // maximum bandwidth (bytes per second) that the connection can use
    STATE,          // current socket state, see UDTSTATUS, read only
    EVENT,          // current available events associated with the socket
    SNDDATA,        // size of data in the sending buffer
    RCVDATA,        // size of data available for recv
    SENDER,         // Sender mode (independent of conn mode), for encryption, tsbpd handshake.
    TSBPDMODE,      // Enable/Disable TsbPd. Enable -> Tx set origin timestamp, Rx deliver packet at origin time + delay
    LATENCY,        // NOT RECOMMENDED. SET: to both RCVLATENCY and PEERLATENCY. GET: same as RCVLATENCY.
    TSBPDDELAY,     // DEPRECATED. ALIAS: LATENCY
    INPUTBW,        // Estimated input stream rate.
    OHEADBW,        // MaxBW ceiling based on % over input stream rate. Applies when UDT_MAXBW=0 (auto).
    PASSPHRASE,     // Crypto PBKDF2 Passphrase size[0,10..64] 0:disable crypto
    PBKEYLEN,       // Crypto key len in bytes {16,24,32} Default: 16 (128-bit)
    KMSTATE,        // Key Material exchange status (UDT_SRTKmState)
    IPTTL,          // IP Time To Live (passthru for system sockopt IPPROTO_IP/IP_TTL)
    IPTOS,          // IP Type of Service (passthru for system sockopt IPPROTO_IP/IP_TOS)
    TLPKTDROP,      // Enable receiver pkt drop
    SNDDROPDELAY,   // Extra delay towards latency for sender TLPKTDROP decision (-1 to off)
    NAKREPORT,      // Enable receiver to send periodic NAK reports
    VERSION,        // Local SRT Version
    PEERVERSION,    // Peer SRT Version (from SRT Handshake)
    CONNTIMEO,      // Connect timeout in msec. Ccaller default: 3000, rendezvous (x 10)
    SNDKMSTATE,     // (GET) the current state of the encryption at the peer side
    RCVKMSTATE,     // (GET) the current state of the encryption at the agent side
    LOSSMAXTTL,     // Maximum possible packet reorder tolerance (number of packets to receive after loss to send lossreport)
    RCVLATENCY,     // TsbPd receiver delay (mSec) to absorb burst of missed packet retransmission
    PEERLATENCY,    // Minimum value of the TsbPd receiver delay (mSec) for the opposite side (peer)
    MINVERSION,     // Minimum SRT version needed for the peer (peers with less version will get connection reject)
    STREAMID,       // A string set to a socket and passed to the listener's accepted socket
    CONGESTION,     // Congestion controller type selection
    MESSAGEAPI,     // In File mode, use message API (portions of data with boundaries)
    PAYLOADSIZE,    // Maximum payload size sent in one UDP packet (0 if unlimited)
    TRANSTYPE,      // Transmission type (set of options required for given transmission type)
    KMREFRESHRATE,  // After sending how many packets the encryption key should be flipped to the new key
    KMPREANNOUNCE,  // How many packets before key flip the new key is annnounced and after key flip the old one decommissioned
    STRICTENC,      // Connection to be rejected or quickly broken when one side encryption set or bad password
    IPV6ONLY,       // IPV6_V6ONLY mode
    PEERIDLETIMEO  // Peer-idle timeout (max time of silence heard from peer) in [ms]
}