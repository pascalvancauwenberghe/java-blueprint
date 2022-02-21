package be.nayima.blueprint.async.generic.usecase;

// Interface for user interface logic that deals with work that is only performed if it is "fresh" (the time to live hasn't expired)
// If the TTL has expired, we get to do some quick processing in drop. For example, we can log a warning
public interface IPerformDroppableWork<Job> {

    void perform(Job in) ;
    void drop(Job in) ;
}
