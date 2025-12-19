package org.example.tp18_grbc.controllers;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.tp18_grbc.entities.Compte;
import org.example.tp18_grbc.services.CompteService;
import org.example.tp18_grbc.stubs.CompteServiceGrpc;
import org.example.tp18_grbc.stubs.GetAllComptesRequest;
import org.example.tp18_grbc.stubs.GetAllComptesResponse;
import org.example.tp18_grbc.stubs.GetCompteByIdRequest;
import org.example.tp18_grbc.stubs.GetCompteByIdResponse;
import org.example.tp18_grbc.stubs.GetTotalSoldeRequest;
import org.example.tp18_grbc.stubs.GetTotalSoldeResponse;
import org.example.tp18_grbc.stubs.SaveCompteRequest;
import org.example.tp18_grbc.stubs.SaveCompteResponse;
import org.example.tp18_grbc.stubs.SoldeStats;
import org.example.tp18_grbc.stubs.TypeCompte;

import java.util.stream.Collectors;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    private final CompteService compteService;

    public CompteServiceImpl(CompteService compteService) {
        this.compteService = compteService;
    }

    private org.example.tp18_grbc.stubs.Compte convertCompteEntityToGrpc(Compte entity) {
        return org.example.tp18_grbc.stubs.Compte.newBuilder()
                .setId(entity.getId())
                .setSolde(entity.getSolde())
                .setDateCreation(entity.getDateCreation())
                .setType(TypeCompte.valueOf(entity.getType()))
                .build();
    }

    @Override
    public void allComptes(GetAllComptesRequest request,
                           StreamObserver<GetAllComptesResponse> responseObserver) {

        var comptes = compteService.findAllComptes().stream()
                .map(this::convertCompteEntityToGrpc)
                .collect(Collectors.toList());

        responseObserver.onNext(
                GetAllComptesResponse.newBuilder()
                        .addAllComptes(comptes)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void compteById(GetCompteByIdRequest request,
                           StreamObserver<GetCompteByIdResponse> responseObserver) {

        Compte entity = compteService.findCompteById(request.getId());

        if (entity != null) {
            var grpcCompte = convertCompteEntityToGrpc(entity);
            responseObserver.onNext(
                    GetCompteByIdResponse.newBuilder()
                            .setCompte(grpcCompte)
                            .build()
            );
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Compte non trouvé avec l'ID: " + request.getId())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void totalSolde(GetTotalSoldeRequest request,
                           StreamObserver<GetTotalSoldeResponse> responseObserver) {

        SoldeStats stats = compteService.getSoldeStats();

        responseObserver.onNext(
                GetTotalSoldeResponse.newBuilder()
                        .setStats(stats)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void saveCompte(SaveCompteRequest request,
                           StreamObserver<SaveCompteResponse> responseObserver) {

        var compteReq = request.getCompte();

        var compteEntity = new Compte();
        compteEntity.setSolde(compteReq.getSolde());
        compteEntity.setDateCreation(compteReq.getDateCreation());
        compteEntity.setType(compteReq.getType().name());

        var savedCompte = compteService.saveCompte(compteEntity);
        var grpcCompte = convertCompteEntityToGrpc(savedCompte);

        responseObserver.onNext(
                SaveCompteResponse.newBuilder()
                        .setCompte(grpcCompte)
                        .build()
        );
        responseObserver.onCompleted();
    }
}
