TTMP Discord Bot
=

**TTMP Discord Bot** is powerful, easy-to-use Discord integration mod for dedicated minecraft servers.

Most notable feature includes:

* Full support for Discord chat formatting, aimed for smoothest user experience
* 100% customizable localization
* Flexible management/usage over multiple text channels
* Extremely powerful scripting support capable of creating any kind of bot commands
  * As well as ability to change every bot commands to your taste
  * ...And build even more complex system using Forge events, all of this with just JavaScript!

<br>

How to set things up?
-
Download the mod and put it in dedicated server. Mod in client side is not required. Launch the server.

After you've successfully launched the server, open the config file named `setup.toml` and put following information.
1. Put your token for Discord bot to `botToken`.
2. You may want to change which language your discord bot would use. In that case, change `locale` to desired locale. Default value is `en_us`(English). Upon setup, Discord bot will collect localization data from specified locale.
   * As of current version, on dedicated server, vanilla localization other than locale `en_us` won't be collected. See **Known Issues** section down below, for more information and how to solve it.
3. Run `/discord setup`.

After this, everything should be set. 

<br>

Umm, can I get some help?
-
**Q. What is 'bot token'?**

Token of the Discord bot is an identifier for bots. It is used while connecting to Discord. [How do you make one, you might ask?](https://github.com/reactiflux/discord-irc/wiki/Creating-a-discord-bot-&-getting-a-token)

**Q. Why I should restart the entire server just to reload `<configname>`? What a stupid mod.**

You don't have to. Use `/discord reload <configname>`. Yes, it includes JavaScript files such as `commands`. Running `/discord reload general` will re-read bot token and reconnect, `/discord reload commands` will refresh all bot commands and update event handlers, and so on.

**Q. The bot listens every single text channels, how can I fix this?**

The simplest solution would be changing the options of undesired text channels so that the bot cannot see the messages. Bots wouldn't show up in member list of channel if they can't see messages on that channel. One side effect, however, is the bot can't send any message to the channel either, because that's how Discord works.

Better option would be in config, `channels.json` in specific. You can set option for each and every text channel, with simple JSON text.

```json
{
  "channelSettings": {
    "1234": {
      "receiveDiscordChat": true,
      "sendMinecraftChat": true,
      "enableDiscordCommands": true
    },
    "5678": {
      "receiveDiscordChat": true,
      "sendMinecraftChat": true,
      "enableDiscordCommands": true
    }
  },
  "defaultChannelSetting": {
    "receiveDiscordChat": true,
    "sendMinecraftChat": false,
    "enableDiscordCommands": true
  }
}
```
Channel specific settings goes into `channelSettings`, each paired with ID of specific channel. `1234` and `5678` is example. ID of channel can be copied by right clicking on the channel -> `Copy ID`. If you don't see the option, enable `Developer Mode` on Discord settings.

Global setting goes into `defaultChannelSetting`. Every channel without channel specific settings uses this setting.

* `receiveDiscordChat`: When enabled, message sent to this channel will be displayed in game.
* `sendMinecraftChat`: When enabled, in-game chat will be sent to this channel.
* `enableDiscordCommands`: When enabled, bot command can be used in this channel.

**Q. The bot doesn't send in-game chat to anywhere, how can I fix this?**

If you read previous answer carefully, you can figure it out this as well ;)

**Q. Can I restore default value for `<configname>`?**

Remove the file(folder, if that's the case), then either restart the server or run `/discord reload <configname>`. You should get freshly generated one.

Keep in mind that *removing the folder* and *removing everything inside the folder* is two different things. If the folder exists, then the bot won't generate any files. Instead it'll read nothing and call it a day.

<br>

Known Issues
-
* On dedicated server, vanilla localization other than locale `en_us` won't be collected. Meaning if you used `en_us` for locale it would work just fine. But if you used `ko_kr` instead, it'll collect all mod added localization data including localizations added by forge, except vanilla ones.
  * The thing you could do, however, is manually collect localization data on client, and transfer that into dedicated server's config.
    1. Install TTMP Discord Bot in client and enter any singleplayer world.
    2. Run `/discord collectLocalization <Your desired locale>`.
    3. Copy the content of `localizations_collected.json`, and paste it into `localizations.json` in dedicated server.
* Dedicated server will stay opened even after server is shutted down normally. It will log nothing, and be closed in few minutes afterwards.
  * It's fine to just ignore and close it.
* Name of some unicode emojis doesn't match between discord, like `:regional_indicator_f:`.

If you're having issue that isn't in this list, Check out the [issues page!](https://github.com/TTMP-Modding-Team/DiscordBot/issues) **Remember to first look around for already existing issues, including closed ones.**