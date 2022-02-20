package be.nayima.blueprint.async.asyncblueprint.usecase;

import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;

public interface IPerformDroppableWork<Job> {

    void perform(Job in) ;
    void drop(Job in) ;
}
