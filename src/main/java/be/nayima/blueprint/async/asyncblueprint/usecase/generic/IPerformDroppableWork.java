package be.nayima.blueprint.async.asyncblueprint.usecase.generic;

public interface IPerformDroppableWork<Job> {

    void perform(Job in) ;
    void drop(Job in) ;
}
