package moe.ku6.rollbit.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import moe.ku6.rollbit.connection.Connection;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class HandlerContext {
    @Getter
    private final Connection connection;
    @Getter @Setter
    private boolean cancelled;

    private final List<Runnable> deferredTasks = new ArrayList<>();

    public void Defer(Runnable task) {
        deferredTasks.add(task);
    }

    public void ExecuteDeferred() {
        deferredTasks.forEach(Runnable::run);
        deferredTasks.clear();
    }
}
