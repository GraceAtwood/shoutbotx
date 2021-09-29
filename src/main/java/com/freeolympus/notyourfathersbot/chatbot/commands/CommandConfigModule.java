package com.freeolympus.notyourfathersbot.chatbot.commands;

import com.freeolympus.notyourfathersbot.chatbot.commands.impl.AddShoutout;
import com.google.inject.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandConfigModule extends AbstractModule {

    @Inject
    @Provides
    @Singleton
    public Map<String, Command> provideCommands(
            Injector injector
    ) {
       return Stream.of(
                injector.getInstance(AddShoutout.class)
        ).collect(Collectors.toMap(Command::getCommand, command -> command));
    }

}
