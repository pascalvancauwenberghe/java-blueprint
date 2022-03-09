package be.nayima.blueprint.async.basicjob.connector;

public interface IExternalParty {
    String call(String request) ;
    int callsMade() ;
}
