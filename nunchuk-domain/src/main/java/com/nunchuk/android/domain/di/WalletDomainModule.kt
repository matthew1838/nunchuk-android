package com.nunchuk.android.domain.di

import com.nunchuk.android.usecase.*
import dagger.Binds
import dagger.Module

@Module
internal interface WalletDomainModule {

    @Binds
    fun bindGetWalletsUseCase(useCase: GetWalletsUseCaseImpl): GetWalletsUseCase

    @Binds
    fun bindCreateWalletUseCase(useCase: CreateWalletUseCaseImpl): CreateWalletUseCase

    @Binds
    fun bindDraftWalletUseCase(useCase: DraftWalletUseCaseImpl): DraftWalletUseCase

    @Binds
    fun bindExportWalletUseCase(useCase: ExportWalletUseCaseImpl): ExportWalletUseCase

    @Binds
    fun bindExportKeystoneWalletUseCase(useCase: ExportKeystoneWalletUseCaseImpl): ExportKeystoneWalletUseCase

    @Binds
    fun bindGetWalletUseCase(useCase: GetWalletUseCaseImpl): GetWalletUseCase

    @Binds
    fun bindUpdateWalletUseCase(useCase: UpdateWalletUseCaseImpl): UpdateWalletUseCase

    @Binds
    fun bindDeleteWalletUseCase(useCase: DeleteWalletUseCaseImpl): DeleteWalletUseCase

    @Binds
    fun bindImportKeystoneWalletUseCase(useCase: ImportKeystoneWalletUseCaseImpl): ImportKeystoneWalletUseCase

    @Binds
    fun bindImportWalletUseCase(useCase: ImportWalletUseCaseImpl): ImportWalletUseCase

    @Binds
    fun bindExportPassportWalletUseCase(useCase: ExportPassportWalletUseCaseImpl): ExportPassportWalletUseCase

}