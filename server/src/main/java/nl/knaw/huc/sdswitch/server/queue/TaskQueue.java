package nl.knaw.huc.sdswitch.server.queue;

import nl.knaw.huc.sdswitch.recipe.RecipeException;
import nl.knaw.huc.sdswitch.recipe.RecipeTask;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskQueue {
    private final Path tasksDir;
    private final ExecutorService executorService;
    private final Map<UUID, String> tasksContentTypes;
    private final Map<UUID, Future<RecipeException>> runningTasks;

    public TaskQueue(Path tasksDir) {
        this.tasksDir = tasksDir;

        int noThreads = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(noThreads);

        tasksContentTypes = new HashMap<>();
        runningTasks = new HashMap<>();
    }

    public String getContentType(UUID uuid) {
        return tasksContentTypes.get(uuid);
    }

    public Future<RecipeException> getTask(UUID uuid) {
        return runningTasks.get(uuid);
    }

    public Path getTaskResult(UUID uuid) {
        return tasksDir.resolve(uuid.toString());
    }

    public UUID addTask(RecipeTask recipeTask, String contentType) {
        UUID uuid = UUID.randomUUID();
        Path resultFile = getTaskResult(uuid);

        Future<RecipeException> submittedTask = executorService.submit(() -> {
            try {
                recipeTask.run(resultFile);
                return null;
            } catch (RecipeException ex) {
                return ex;
            }
        });

        tasksContentTypes.put(uuid, contentType);
        runningTasks.put(uuid, submittedTask);

        return uuid;
    }
}