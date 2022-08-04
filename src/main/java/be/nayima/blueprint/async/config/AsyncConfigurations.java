package be.nayima.blueprint.async.config;

import be.nayima.blueprint.async.basicjob.processor.BasicJobStreamConfig;
import be.nayima.blueprint.async.batch.processor.BatchStreamConfig;
import be.nayima.blueprint.async.generic.processor.QueueFunctionDefinition;
import be.nayima.blueprint.async.persistent.processor.PersistentStreamConfig;

import java.util.ArrayList;

public class AsyncConfigurations {
    public static ArrayList<QueueFunctionDefinition> gatherConfigurations() {
        var functionDefinitions = new ArrayList<QueueFunctionDefinition>();

        functionDefinitions.addAll(BasicJobStreamConfig.allFunctions());
        functionDefinitions.addAll(PersistentStreamConfig.allFunctions());
        functionDefinitions.addAll(BatchStreamConfig.allFunctions());

        return functionDefinitions;
    }
}
