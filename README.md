プラグインを起動させた最初の段階では動きません。
-

config.ymlのDatabaseの項目を正しく入力し、最後にuseをtrueにすることで動作。

```
Database:
  use: false
```
依存関係にLibsDisguisesとpacketevents、ItemLoreLibが必要、MythicMobsは任意
-------------------------------------------------------------------

EntityOptions.UseMythicMobsの項目は、NPCとしてMMのmobを使用するかのオプションです。
使用する場合、trueにした上でmobを作成しFactionに「NPCShop」を含む必要があります。

Faction: NPC_Shop　や、 Faction: NPCShop_Test などでも可能。

mobを作成後、スポナーを設置すればショップとして理由できます。

MM式のショップとNPCShopのショップでは違いが1つあり、
EntityOptions.DefaultsDisplayNameの項目はNPCShop製のMobにのみ有効です。
ネームタグをメインハンドで持って、クリエで対象を選択すると名前を変更可能です。

Shopの陳列アイテムの追加方法は、/shop itemEditor から専用のデータを付与したアイテムを使用します。
追加法は以下の2つで、
・クリエで対象をShiftクリックしたときにメインハンドに専用アイテムを持っている場合はそのアイテムを直接追加します。
・クリエで対応をクリックし、取引メニューを直接弄ってアイテムを追加します。
```
EntityOptions:
  UseMythicMobs: false
  DefaultsDisplayName: "&b&lNPCShop"
```
-------------------------------------------------------------------

また、ShopOptions.MaxStackPerItemは一つのアイテムに格納できる取引要求素材の種類になります。
1stを超えて追加する場合は別の種類としてカウントされます。
```
ShopOptions:
  MaxStackPerItem: 20
```
-------------------------------------------------------------------

現在はアイテムのみを取引要求ですが、Vault通貨を追加予定。
ShopTradeのログを残す機能を実装予定。
Shopを別の場所に移す機能を実装予定です。
