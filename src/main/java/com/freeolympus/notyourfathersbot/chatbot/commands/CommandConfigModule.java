package com.freeolympus.notyourfathersbot.chatbot.commands;

import com.freeolympus.notyourfathersbot.chatbot.commands.impl.*;
import com.google.inject.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                injector.getInstance(GetInterval.class),
                injector.getInstance(ListShoutouts.class),
                injector.getInstance(SetInterval.class),
                injector.getInstance(TriggerShoutout.class)
        );
    }
}
