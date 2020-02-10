package bei7473p5254d69jcuat.tenyu.communication.request;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 * どうやら通信系の処理は一連のメッセージ交換がたびたび必要になり、
 * その一連の通信を1メソッドのように捉える観点が有効で、
 * いくつかのメッセージクラスは一連の通信の開始メッセージとなる。
 * そのようなメッセージクラスはstatic send()を持っている。
 * この空インターフェースはただそのようなメッセージクラスのメモで、
 * 動作上記述する必要性は無い。
 *
 * シーケンスではない単発のメッセージクラスはこれがついている場合とついていない場合がある。
 * メッセージの作成から送信までの流れが呼び出し元固有の面が強い場合など。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@java.lang.annotation.Retention(SOURCE)
@Target({ TYPE })
public @interface RequestSequenceStart {

}
