package be.nayima.blueprint.async.asyncblueprint.usecase;

public interface IPerformDroppableWork<Job> {

    void perform(Job in) ;
    void drop(Job in) ;
}
