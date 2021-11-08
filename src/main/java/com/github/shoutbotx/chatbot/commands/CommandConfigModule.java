package com.github.shoutbotx.chatbot.commands;

import com.github.shoutbotx.chatbot.commands.impl.*;
import com.google.inject.*;

import java.util.List;

public class CommandConfigModule extends AbstractModule {

    @Inject
    @Provides
    @Singleton
    public List<Command> provideCommands(
            Injector injector
    ) {
        return List.of(
                injector.getInstance(AddShoutout.class),
                injector.getInstance(DeleteShoutout.class),
                injector.getInstance(EditShoutoutSetting.class),
                injector.getInstance(ListShoutouts.class),
                injector.getInstance(TriggerShoutout.class)
        );
    }
}
