package bei7473p5254d69jcuat.tenyu.communication.local;

import com.github.arteam.simplejsonrpc.core.annotation.*;

@JsonRpcError(code = -1, message = "exception")
public class LocalIpcAPIException extends Exception {
}