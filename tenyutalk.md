# 概要
Tenyu基盤ソフトウェアに実装されるTenyutalkというシステムについて説明する。TenyutalkはWWWを置き換えれる事を設計目標としたP2Pシステム。

# 私
https://github.com/lifeinwild/tenyu#%E7%A7%81

# 目次
<!-- TOC -->

- [概要](#概要)
- [私](#私)
- [目次](#目次)
- [一般用語](#一般用語)
    - [Web](#web)
    - [HTML](#html)
    - [Webブラウザ](#webブラウザ)
    - [JavaScript](#javascript)
    - [Git](#git)
    - [Java](#java)
    - [JGit](#jgit)
    - [JavaFX](#javafx)
- [独自用語ないし解釈](#独自用語ないし解釈)
    - [ハイパーテキスト](#ハイパーテキスト)
- [動機](#動機)
    - [WWWの問題](#wwwの問題)
    - [Tenyutalkのメリット](#tenyutalkのメリット)
- [内容](#内容)
    - [アプレット](#アプレット)
    - [公開日時証明](#公開日時証明)
    - [クラス設計](#クラス設計)
        - [Tenyuリポジトリ](#tenyuリポジトリ)
        - [Tenyuアーティファクト](#tenyuアーティファクト)
        - [Tenyuアーティファクトバージョン別情報](#tenyuアーティファクトバージョン別情報)
    - [バージョニング](#バージョニング)
- [ノード別管理情報](#ノード別管理情報)
    - [Tenyuリポジトリのフォルダ構造](#tenyuリポジトリのフォルダ構造)
    - [作業フォルダ](#作業フォルダ)
    - [リリース準備フォルダ](#リリース準備フォルダ)
    - [リリースフォルダ](#リリースフォルダ)
    - [Git型Tenyuリポジトリ](#git型tenyuリポジトリ)
    - [Tenyuプレイイングリポジトリ](#tenyuプレイイングリポジトリ)
    - [Tenyuアプレットの動作フォルダ](#tenyuアプレットの動作フォルダ)
    - [フリーフォルダ](#フリーフォルダ)
- [参照](#参照)
    - [フレキシブル参照](#フレキシブル参照)
    - [セキュア参照](#セキュア参照)
- [その他](#その他)
    - [TenyuとTenyutalk](#tenyuとtenyutalk)
    - [SmalltalkとTenyutalk](#smalltalkとtenyutalk)

<!-- /TOC -->
# 一般用語
## Web
HTMLを用いたネット上の文書連携  
https://ja.wikipedia.org/wiki/World_Wide_Web

## HTML
[ハイパーテキスト](#ハイパーテキスト)を記述する言語。

## Webブラウザ
[ハイパーテキスト](#ハイパーテキスト)を表示するソフトウェア。ハイパーテキストの表示の際、ハイパーテキスト中の画像リンク等を辿って多数のサーバアクセスを行い表示している。その他[JavaScript](#JavaScript)の実行など。

## JavaScript
プログラミング言語で、主に[ハイパーテキスト](#ハイパーテキスト)上でより高機能な表現をするために使用される。[Webブラウザ](#Webブラウザ)で標準的に採用されている。

## Git
制作物の変更履歴を管理できる。  
他の人が自分の制作物に修正を加えるための仕組みにもなる。  
https://ja.wikipedia.org/wiki/Git

## Java
write once, run anywhere  
完全には実現されていないがその点でこれより優れたものは恐らく無い。  
https://ja.wikipedia.org/wiki/Java

## JGit
JGitによってJavaは[Git](#Git)と連携したシステムが作れます。  
https://git-scm.com/book/ja/v2/Appendix-B%3A-Git%E3%82%92%E3%81%82%E3%81%AA%E3%81%9F%E3%81%AE%E3%82%A2%E3%83%97%E3%83%AA%E3%82%B1%E3%83%BC%E3%82%B7%E3%83%A7%E3%83%B3%E3%81%AB%E7%B5%84%E3%81%BF%E8%BE%BC%E3%82%80-JGit

## JavaFX
JavaFXは通常のGUIから3Dゲームまで対応可能なGUIフレームワークで、画像を表示したり動画を再生する機能も提供されている。  
https://docs.oracle.com/javafx/2/api/javafx/scene/media/package-summary.html#SupportedMediaTypes

# 独自用語ないし解釈
## ハイパーテキスト
文字と紙が発明され普及して以降、文明は紙の文書によって実現されてきた。

もし文書が無ければ口頭の会話と生身の記憶力で社会を実現する必要があり、個々人を社会的に捕捉するという前提の上では可能な人口が制限される。

つまり今日のような高度な社会は紙の文書のおかげだった。

そのような観点に立つと、もしITを用いて文書という概念が紙の文書より強力なものになったら、文明が根本から変わるのではないだろうかと思わせる。

そして現代にはハイパーテキストがある。ハイパーテキストはハイパーリンクで他のハイパーテキストに飛べる。あるいは文書中に画像や動画を埋め込んだりできる。

さらに、アクセスした人に応じて文書の内容が変わったり、リアルタイムに情報を表示していたりする。この辺はハイパーテキストというよりサーバのおかげだがハイパーテキストベースのWWWが存在するからWebブラウザが存在し、そしてWebサーバが流行った。

文明を変化させるという期待は少し大げさだったかもしれないが、情報の表現と収集は効率化した。

ハイパーテキストはHTMLで記述されてきたが、近年ではより簡素化したMarkdownという記法が流行しつつある。この文書もMarkdownで記述されている。

# 動機

## WWWの問題
- HTMLのセマンティクスはJavaのクラスより劣る。これによって検索エンジンやサイト間連携に限界が生じる。
- 多数のサイトがあり、他のサイトのDBを参照できない。例えアクセスできても情報の表現方法に差異がありスムーズに連携できない（設計の非統一）。連携する動機が無い（ビジネスモデルの問題）。
- 各サイトで似たようなプログラム（ユーザー登録やコメントシステム等）が作成されている。

## Tenyutalkのメリット
- [アプレット](#アプレット)
- [公開日時証明](#公開日時証明)
- P2P。[アプレット](#アプレット)はエンドユーザーのPCで実行されるので、アプレットが他のアプレットにDBや機能を提供してもその連携のためにサーバ負荷が高まるということが無い。
- Tenyutalk上の全制作活動にGitを提供し、フォークとプルリクの流れを可能にする。（ただし大規模開発を想定しない）※未実装

# 内容

## アプレット
これらの特徴から、Tenyutalkはある種の新しいプログラミング環境です。
- アプレットはTenyu基盤ソフトウェア上で動作するのでTenyu基盤ソフトウェアのクラスを利用できる。
- 誰でもアプレットを作成して他の人に提供できる。Tenyuのレーティングゲーム等もアプレットとして作成される。
- アプレットはTenyu基盤ソフトウェアのユーザーDBなど既成のDBを利用できる。
- 任意のユーザーと通信できる等既成のネットワークを利用できる。
- 他のアプレットが作成したDBや機能に（それが公開設定なら）アクセスできる。

## 公開日時証明
[リリースフォルダ](#リリースフォルダ)に置かれたファイルやフォルダまたはgitローカルリポジトリのコミットのSHA1に対する電子署名を各ノードは提供する。

- ハッシュ署名。P2Pネットワークに公開する時、公開直前に自動的にいくつかのノードにハッシュ値を送り電子署名を得る。
- 読み取り署名。ユーザーが他者の制作物を見た時に署名数が最大に達してない限り自動的に署名される。

その署名一覧が公開日時証明になる。

注意点。多人数の結託によってある程度の数までは署名日時を捏造できてしまうので、**大勢の多様なユーザーによる署名があって初めて信憑性がある**。

## クラス設計
[Tenyuリポジトリ](#Tenyuリポジトリ)＞[Tenyuアーティファクト](#Tenyuアーティファクト)＞[Tenyuアーティファクトバージョン別情報](#Tenyuアーティファクトバージョン別情報)

### Tenyuリポジトリ
例えばyoutubeのchのような様々な成果物を置く場所。
多数のアーティファクトと紐つくもの。ミラーノードを作成可能。

[Tenyuリポジトリ](#Tenyuリポジトリ)の一部メタデータは客観でどのノードも持ちますが、[Tenyuリポジトリ](#Tenyuリポジトリ)のフォルダやファイルは一部のノード（アップロード者、ミラーノード等）が持ちます。

### Tenyuアーティファクト
１種類の成果物。多数のバージョン別情報と紐つく。

Markdown記事、画像、動画、アプレット、ライブラリ等ほぼあらゆるソフトウェア。文脈によってアーティファクトとも呼ばれる。

### Tenyuアーティファクトバージョン別情報
成果物の各バージョン毎の情報。ハッシュ値、[バージョン番号](#バージョニング)、サイズ、署名など。

## バージョニング
semverに沿っています。
https://semver.org/

後方互換性が無い変化はmajorバージョンを変化させますが、もし説明やタグが変化するほど大きな変化（検索性の変化）ならバージョンの変化ではなくアーティファクトを新しく作るべきです。

buildは任意の英数字が使えるので、別ファイルでいくつかビルドの方法を説明しておいてそのうちの１つのビルド方法の名前にする事で、例えば特定のモジュールが除外されたエディションとか、全モジュールが含められたフルバージョンとかを表現できます。buildの説明にビルドに用いた開発ツールの名前とバージョンを含めればreproducible buildsのためにも使えそうです。

# ノード別管理情報
フォルダやファイルは、ハッシュ値等のメタデータは客観DB管理となるが実体はノード別管理となる。

## Tenyuリポジトリのフォルダ構造
フォルダアーキテクチャ
- [Tenyuリポジトリ](#Tenyuリポジトリ)
  - [作業フォルダ](#作業フォルダ)
    - （[Git型Tenyuリポジトリ](#Git型Tenyuリポジトリ)の場合）[gitローカルリポジトリ](#gitローカルリポジトリ)
  - [リリースフォルダ](#リリースフォルダ)

２種類に分かれる
- [Git型Tenyuリポジトリ](#Git型Tenyuリポジトリ)
- [Tenyuプレイイングリポジトリ](#Tenyuプレイイングリポジトリ)

## 作業フォルダ
[Tenyuリポジトリ](#Tenyuリポジトリ)直下に作成される**working**というフォルダ。

[作業フォルダ](#作業フォルダ)はgitの文脈ではワークツリー、ワーキングエリア、作業ツリーとも言われます。全て同じものを指しています。

任意の制作ツールによる任意のファイルを置けます。プログラミングのIDEが作成するソースコードやプロジェクトファイル、動画編集ソフトが作成するプロジェクトファイルや使用する素材一覧等です。Markdown記事とそれが参照する画像等も置けます。

## リリース準備フォルダ
**releaseTmp**フォルダ。
GUI上での成果物公開操作においてこのフォルダに公開したい成果物を置きます。そしてバージョン番号や直前のバージョンを指定します。

成果物は１ファイルまたはフォルダどちらでも可能です。

[作業フォルダ](#作業フォルダ)上で開発ツール等を通してビルド処理をして、その出力（成果物）をreleaseTmpに置きます。tenyuは成果物公開操作に伴い自動的にバージョン文字列を付加してリリースフォルダにコピーします。ただし設置されたファイルが既にバージョニングされている場合新たにバージョンを付加しません。

## リリースフォルダ
[Tenyuリポジトリ](#Tenyuリポジトリ)直下に作成される**release**というフォルダ。[リリースフォルダ](#リリースフォルダ)**直下**に置かれるファイルまたはフォルダを[成果物](#成果物)と呼びます。

[リリース準備フォルダ](#リリース準備フォルダ)に成果物を設置してGUI上で成果物公開操作を行うとリリースフォルダにバージョン番号が付与された状態で設置されます。

リリースフォルダに置かれたファイルは修正してはいけません。新しいバージョンは別ファイル（フォルダ）として別途設置されますが、古いバージョンは修正されず残り続けます。このルールは成果物を相互参照するシステムを構想すると必要になります。つまりある成果物が他の成果物に依存している場合、他の成果物がアップデートで変化した時、特定のバージョンを指定して依存していれば（そして古いバージョンが不変なら）意味を破壊されずに済むという事です。

## Git型Tenyuリポジトリ
ライブラリやアプレットや画像系素材等、制作物を設置するリポジトリ。
できるだけ作者を捕捉できるようにするため、アップロード者やアップロード日時等をP2Pプラットフォーム上で証明する。

[作業フォルダ](#作業フォルダ)に[gitローカルリポジトリ](#gitローカルリポジトリ)が置かれtenyuのGUI上からローカルリポジトリにコミットできます。

[Git](#Git)は一般的な技術です。

これはgitの仕様ですが.gitignoreにコミットにおいて無視されるファイルの条件を記述できます。例えば一部のテキストエディタは.bakなどバックアップファイルをテキストファイルと同じフォルダに残しますが、そのようなファイルはコミットする必要が無いので.gitignoreによって排除すべきです。

**.git**フォルダが置かれgitローカルリポジトリが管理されます。

[Git型Tenyuリポジトリ](#Git型Tenyuリポジトリ)のgitローカルリポジトリは他のユーザーによってcloneされる可能性があります。未実装なので現状公開されません。

## Tenyuプレイイングリポジトリ
アプレットはそれを実行しているユーザーに新たなアーティファクトを作成させれます。そのアーティファクトとしてファイルを

## Tenyuアプレットの動作フォルダ
[アプレット](#アプレット)が実行時に作成するDB等が置かれます。

このフォルダの内容は公開されません。しかしアプレットの動作を通じてデータが公開される可能性があります。

## フリーフォルダ
単純なファイル倉庫。客観DB上にメタデータが登録されない。公開日時証明が行われない。
設置されたファイルは即座にP2Pネットワーク上でアクセス可能になる。

**ここに創作的成果を登録してはならない**。

例えばアプレット等が自動的にファイルを作成できるので、ゲームアプレットがリプレイファイルの出力先にできる。
他には配信アプレットがアーカイブとして動画とチャットログを出力するなどが考えられる。

# 参照
Tenyuは様々なオブジェクト（User等）を扱いますが、それらオブジェクトは参照オブジェクトを通じて参照できます。

Tenyutalkシステム上の[成果物](#成果物)も参照できます。

参照オブジェクトはURL表現を作成できてMarkdownの記事上でハイパーリンクとして使用できます。参照オブジェクトは参照先オブジェクトのmedia typeを持ち、一部のmedia typeではMarkdown記事上で自動的に参照先が取得されて表示されます。ちょうどHTMLのIMGタグのように。

以下の仕組みによって参照先オブジェクトを管理しているノードがオフラインになっても多くの場合参照できます。
- 参照は参照先オブジェクトをミラーしているノードリスト等の情報を持ちアップロード者ノードがオフラインならミラーノードに問い合わせます。
- アップロード者ノードもミラーノードもオフラインの場合、近傍に問い合わせます。
- 一度取得したオブジェクトは自分のノードでミラーされます。

参照先がフォルダで表現されている成果物だったらそのフォルダ以下の全ファイルフォルダがDLされます。

## フレキシブル参照
参照のバージョン指定において例えばマイナーとパッチを未指定にして常に最新のものを指定できます。

## セキュア参照
参照オブジェクトが参照先オブジェクトのハッシュ値を持ち、事前に指定されたファイルを取得します。

# その他
## TenyuとTenyutalk
- 内部的にDBが分かれていてトランザクションの範囲が分離されています。
- Tenyuは分散合意を活用し全ノードで共通のDBを持つ事を主眼としたプラットフォームであり、Tenyutalkは各ノード毎に異なるDBやファイルを構築します。
- Tenyuの構想や基礎技術は２０１７年には一通り完成していましたが、Tenyutalkは２０１８年以降に思いついたものです。レーティングゲーム等のユーザーが作成した成果物を登録する仕組みを考えているうちに、創作物を発表するプラットフォームとして包括的な仕組みが必要だと思いました。
- Tenyuは分散合意とプロセッサ証明という基礎技術を発明したのでそれを活用してみようというのが動機として強く、一方でTenyutalkはP2Pプラットフォームについて考えたりSmalltalkについて調べたりしているうちにWWWを置き換えれる可能性が見えてきて取り組み始めたものです。

## SmalltalkとTenyutalk
内容的には全く違います。なぜSmalltalkにあやかった名前にしているのかですが、Smalltalkは私の印象では「様々なものを包括的・統合的に扱っている特殊なプログラミング環境」というものでした。

各要素を分離するのではなく、統合的・包括的な方に向かう、**独創的な環境**を創出しようとするという点では一致していると思います。