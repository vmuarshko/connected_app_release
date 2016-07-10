package com.cookoo.life.btdevice;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stevi.deter on 3/11/14.
 */
public class GattErrorCodes {

    /* Success code and error codes */
    public final static int GATT_SUCCESS = 0x0000;
    public final static int GATT_INVALID_HANDLE = 0x0001;
    public final static int GATT_READ_NOT_PERMIT = 0x0002;
    public final static int GATT_WRITE_NOT_PERMIT = 0x0003;
    public final static int GATT_INVALID_PDU = 0x0004;
    public final static int GATT_INSUF_AUTHENTICATION = 0x0005;
    public final static int GATT_REQ_NOT_SUPPORTED = 0x0006;
    public final static int GATT_INVALID_OFFSET = 0x0007;
    public final static int GATT_INSUF_AUTHORIZATION = 0x0008;
    public final static int GATT_PREPARE_Q_FULL = 0x0009;
    public final static int GATT_NOT_FOUND = 0x000a;
    public final static int GATT_NOT_LONG = 0x000b;
    public final static int GATT_INSUF_KEY_SIZE = 0x000c;
    public final static int GATT_INVALID_ATTR_LEN = 0x000d;
    public final static int GATT_ERR_UNLIKELY = 0x000e;
    public final static int GATT_INSUF_ENCRYPTION = 0x000f;
    public final static int GATT_UNSUPPORT_GRP_TYPE = 0x0010;
    public final static int GATT_INSUF_RESOURCE = 0x0011;


    public final static int GATT_ILLEGAL_PARAMETER = 0x0087;
    public final static int GATT_NO_RESOURCES = 0x0080;
    public final static int GATT_INTERNAL_ERROR = 0x0081;
    public final static int GATT_WRONG_STATE = 0x0082;
    public final static int GATT_DB_FULL = 0x0083;
    public final static int GATT_BUSY = 0x0084;
    public final static int GATT_ERROR = 0x0085;
    public final static int GATT_CMD_STARTED = 0x0086;
    public final static int GATT_PENDING = 0x0088;
    public final static int GATT_AUTH_FAIL = 0x0089;
    public final static int GATT_MORE = 0x008a;
    public final static int GATT_INVALID_CFG = 0x008b;
    public final static int GATT_SERVICE_STARTED = 0x008c;
    public final static int GATT_ENCRYPED_MITM = GATT_SUCCESS;
    public final static int GATT_ENCRYPED_NO_MITM = 0x008d;
    public final static int GATT_NOT_ENCRYPTED = 0x008e;

    public final static Map<Integer,String> GATT_CODE_MESSAGES;
    static {
        GATT_CODE_MESSAGES = new HashMap<Integer,String>();
        GATT_CODE_MESSAGES.put(GATT_SUCCESS,"SUCCESS");
        GATT_CODE_MESSAGES.put(GATT_INVALID_HANDLE,"Invalid Handle");
        GATT_CODE_MESSAGES.put(GATT_READ_NOT_PERMIT,"Read not permitted");
        GATT_CODE_MESSAGES.put(GATT_WRITE_NOT_PERMIT,"Write not permitted");
        GATT_CODE_MESSAGES.put(GATT_INVALID_PDU,"Invalid PDU");
        GATT_CODE_MESSAGES.put(GATT_INSUF_AUTHENTICATION,"Insufficient Authentication");
        GATT_CODE_MESSAGES.put(GATT_REQ_NOT_SUPPORTED,"Req not supported");
        GATT_CODE_MESSAGES.put(GATT_INVALID_OFFSET,"Invalid offset");
        GATT_CODE_MESSAGES.put(GATT_INSUF_AUTHORIZATION,"Insufficient Authorization");
        GATT_CODE_MESSAGES.put(GATT_PREPARE_Q_FULL,"Prepare queue full");
        GATT_CODE_MESSAGES.put(GATT_NOT_FOUND,"not found");
        GATT_CODE_MESSAGES.put(GATT_NOT_LONG,"not long");
        GATT_CODE_MESSAGES.put(GATT_INSUF_KEY_SIZE,"insufficient key size");
        GATT_CODE_MESSAGES.put(GATT_INVALID_ATTR_LEN,"invalid attribute length");
        GATT_CODE_MESSAGES.put(GATT_ERR_UNLIKELY,"error unlikely");
        GATT_CODE_MESSAGES.put(GATT_INSUF_ENCRYPTION,"insufficient encryption");
        GATT_CODE_MESSAGES.put(GATT_UNSUPPORT_GRP_TYPE,"unsupported GRP type");
        GATT_CODE_MESSAGES.put(GATT_INSUF_RESOURCE,"insufficient resource");
        GATT_CODE_MESSAGES.put(GATT_ILLEGAL_PARAMETER,"illegal parameter");
        GATT_CODE_MESSAGES.put(GATT_NO_RESOURCES,"no resources");
        GATT_CODE_MESSAGES.put(GATT_INTERNAL_ERROR,"internal error");
        GATT_CODE_MESSAGES.put(GATT_WRONG_STATE,"wrong state");
        GATT_CODE_MESSAGES.put(GATT_DB_FULL,"DB Full");
        GATT_CODE_MESSAGES.put(GATT_BUSY,"Busy");
        GATT_CODE_MESSAGES.put(GATT_ERROR,"Error");
        GATT_CODE_MESSAGES.put(GATT_CMD_STARTED,"Command Started");
        GATT_CODE_MESSAGES.put(GATT_PENDING,"Pending");
        GATT_CODE_MESSAGES.put(GATT_AUTH_FAIL,"Auth fail");
        GATT_CODE_MESSAGES.put(GATT_MORE,"More");
        GATT_CODE_MESSAGES.put(GATT_INVALID_CFG,"Invalid Config");
        GATT_CODE_MESSAGES.put(GATT_SERVICE_STARTED,"Service started");
        GATT_CODE_MESSAGES.put(GATT_ENCRYPED_MITM,"ENCRYPED MITM");
        GATT_CODE_MESSAGES.put(GATT_ENCRYPED_NO_MITM,"ENCRYPED NO MITM");
        GATT_CODE_MESSAGES.put(GATT_NOT_ENCRYPTED,"Not encrypted");

    }
}
