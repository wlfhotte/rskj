/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.config;

import co.rsk.bitcoinj.core.BtcECKey;
import co.rsk.bitcoinj.core.Coin;
import co.rsk.bitcoinj.core.NetworkParameters;
import co.rsk.peg.AddressBasedAuthorizer;
import co.rsk.peg.Federation;
import co.rsk.peg.FederationMember;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.crypto.ECKey;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BridgeTestNetConstants extends BridgeConstants {
    private static BridgeTestNetConstants instance = new BridgeTestNetConstants();

    BridgeTestNetConstants() {
        btcParamsString = NetworkParameters.ID_TESTNET;

        BtcECKey federator0PublicKey = BtcECKey.fromPublicOnly(Hex.decode("039a060badbeb24bee49eb2063f616c0f0f0765d4ca646b20a88ce828f259fcdb9"));
        BtcECKey federator1PublicKey = BtcECKey.fromPublicOnly(Hex.decode("02afc230c2d355b1a577682b07bc2646041b5d0177af0f98395a46018da699b6da"));
        BtcECKey federator2PublicKey = BtcECKey.fromPublicOnly(Hex.decode("0344a3c38cd59afcba3edcebe143e025574594b001700dec41e59409bdbd0f2a09"));
        BtcECKey federator3PublicKey = BtcECKey.fromPublicOnly(Hex.decode("034844a99cd7028aa319476674cc381df006628be71bc5593b8b5fdb32bb42ef85"));

        List<BtcECKey> genesisFederationPublicKeys = Arrays.asList(federator0PublicKey, federator1PublicKey, federator2PublicKey, federator3PublicKey);

        // IMPORTANT: BTC, RSK and MST keys are the same.
        // Change upon implementation of the <INSERT FORK NAME HERE> fork.
        List<FederationMember> federationMembers = FederationMember.getFederationMembersFromKeys(genesisFederationPublicKeys);

        // Currently set to:
        // Currently set to: Monday, October 8, 2018 12:00:00 AM GMT-03:00
        Instant genesisFederationAddressCreatedAt = Instant.ofEpochMilli(1538967600l);

        genesisFederation = new Federation(
                federationMembers,
                genesisFederationAddressCreatedAt,
                1L,
                getBtcParams()
        );

        btc2RskMinimumAcceptableConfirmations = 3;
        btc2RskMinimumAcceptableConfirmationsOnRsk = 10;
        rsk2BtcMinimumAcceptableConfirmations = 10;

        updateBridgeExecutionPeriod = 3 * 60 * 1000; // 3 minutes

        maxBtcHeadersPerRskBlock = 500;

        minimumLockTxValue = Coin.valueOf(1_000_000); // 0.1 tBTC
        minimumReleaseTxValue = Coin.valueOf(500_000); // 0.05 tBTC

        // Passphrases are kept private
        List<ECKey> federationChangeAuthorizedKeys = Arrays.stream(new String[]{
                "04fe0881821ea20b7a7ae69429e05a6baeda2460778ab6b24b51fc92ff69656fa5033b8789505df7f25477a89042554bfbd0834364592bd0ae18ee1a1d5d87b758", // seed: fed-01
                "04394f1ecdc50498b8e5d4beba3f3612e97c74e56e5f90426df74b2fc074ecbde892f85de7b7dedbcd142c3178485432032fdaf890bc687e5bdb870010a401bf27", // seed: fed-02
                "04a2acc68ec45a1cde42508b26dda8b67843c346fcc2b33866d806fb82c5642b2bcadb46430e1d52b1fbe462383199dc756807fb27e05887e97cd67fe9d84baf06"  // seed: fed-03
        }).map(hex -> ECKey.fromPublicOnly(Hex.decode(hex))).collect(Collectors.toList());

        federationChangeAuthorizer = new AddressBasedAuthorizer(
                federationChangeAuthorizedKeys,
                AddressBasedAuthorizer.MinimumRequiredCalculation.MAJORITY
        );

        List<ECKey> lockWhitelistAuthorizedKeys = Arrays.stream(new String[]{
                "0496aebf9a84b1081c2551cb167b99d0d29e3afd8c41559c4f8fef48c64d802a7f7bb86009ca2fce169ae6101d7a3ef1f5b98712dcd15cebb3e2a6315c8886e07a" // seed: whitelist-authorizer
        }).map(hex -> ECKey.fromPublicOnly(Hex.decode(hex))).collect(Collectors.toList());

        lockWhitelistChangeAuthorizer = new AddressBasedAuthorizer(
                lockWhitelistAuthorizedKeys,
                AddressBasedAuthorizer.MinimumRequiredCalculation.ONE
        );

        federationActivationAge = 20L;

        fundsMigrationAgeSinceActivationBegin = 20L;
        fundsMigrationAgeSinceActivationEnd = 60L;

        List<ECKey> feePerKbAuthorizedKeys = Arrays.stream(new String[]{
                "0497f41f14a77f88f6d09434fcf56bd8f66716deaa4c29127d54531fbc3b4c0a67657c9a9c72dbc91c710f350d787ea0574e96d9252f3862d27634b0759582a109", // seed: feeperkb-authorizer-01
                "04243180c9b18571440db278448a8882fdab39c0a002ca480937cce4fc4620253e67997c89cb9ca7804a3cbaed4e65e9391c8f466c2a98bb92e8a966d45101462c", // seed: feeperkb-authorizer-02
                "04dcb3132beafe8fbfa28a3e417c44a406263b09be9b706e300beced21d0c3bae94369dcca7edcbcf862313d3850d7e2d03afdaaebc8e29e52a021634e32ee792f"  // seed: feeperkb-authorizer-03
        }).map(hex -> ECKey.fromPublicOnly(Hex.decode(hex))).collect(Collectors.toList());

        feePerKbChangeAuthorizer = new AddressBasedAuthorizer(
                feePerKbAuthorizedKeys,
                AddressBasedAuthorizer.MinimumRequiredCalculation.MAJORITY
        );

        genesisFeePerKb = Coin.MILLICOIN;

        maxFeePerKb = Coin.valueOf(5_000_000L);

        List<ECKey> increaseLockingCapAuthorizedKeys = Arrays.stream(new String[]{
                "0415e032ab84a670d848f80620efb6c9b2a3d8a9159fd17adc780e0ae1da8785a485f5e4c5d9eead55008695884970484e85942872e289ed42f75fbcdc7c8eac8b" // seed: lockingcap-authorizer
        }).map(hex -> ECKey.fromPublicOnly(Hex.decode(hex))).collect(Collectors.toList());

        increaseLockingCapAuthorizer = new AddressBasedAuthorizer(
                increaseLockingCapAuthorizedKeys,
                AddressBasedAuthorizer.MinimumRequiredCalculation.ONE
        );

        lockingCapIncrementsMultiplier = 2;
        initialLockingCap = Coin.COIN.multiply(950); // 950 tBTC
    }

    public static BridgeTestNetConstants getInstance() {
        return instance;
    }

}
