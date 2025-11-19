-- Sample HealBot SavedVariables file for testing
HealBot_Config = {
    ["Spells"] = {
        ["Priest@RealmName"] = {
            ["Default"] = {
                ["EnabledKeyCombo"] = {
                    ["Ctrl+Left"] = {
                        ["SpellName"] = "Flash Heal",
                        ["SpellID"] = 2061,
                        ["Button"] = 1,
                        ["TargetType"] = "target"
                    },
                    ["Shift+Left"] = {
                        ["SpellName"] = "Power Word: Shield",
                        ["SpellID"] = 17,
                        ["Button"] = 1,
                        ["TargetType"] = "target"
                    },
                    ["Alt+Left"] = {
                        ["SpellName"] = "Renew",
                        ["SpellID"] = 139,
                        ["Button"] = 1,
                        ["TargetType"] = "target"
                    },
                    ["Right"] = {
                        ["SpellName"] = "Greater Heal",
                        ["SpellID"] = 2060,
                        ["Button"] = 2,
                        ["TargetType"] = "target"
                    },
                    ["Ctrl+Right"] = {
                        ["SpellName"] = "Prayer of Mending",
                        ["SpellID"] = 33076,
                        ["Button"] = 2,
                        ["TargetType"] = "target"
                    }
                }
            },
            ["Raid"] = {
                ["EnabledKeyCombo"] = {
                    ["Left"] = {
                        ["SpellName"] = "Flash Heal",
                        ["SpellID"] = 2061,
                        ["Button"] = 1,
                        ["TargetType"] = "target"
                    },
                    ["Middle"] = {
                        ["SpellName"] = "Dispel Magic",
                        ["SpellID"] = 527,
                        ["Button"] = 3,
                        ["TargetType"] = "target"
                    },
                    ["Shift+Middle"] = {
                        ["SpellName"] = "Purify",
                        ["SpellID"] = 527,
                        ["Button"] = 3,
                        ["TargetType"] = "target"
                    }
                }
            }
        },
        ["Paladin@RealmName"] = {
            ["Default"] = {
                ["EnabledKeyCombo"] = {
                    ["Left"] = {
                        ["SpellName"] = "Holy Light",
                        ["SpellID"] = 635,
                        ["Button"] = 1,
                        ["TargetType"] = "target"
                    },
                    ["Right"] = {
                        ["SpellName"] = "Flash of Light",
                        ["SpellID"] = 19750,
                        ["Button"] = 2,
                        ["TargetType"] = "target"
                    },
                    ["Ctrl+Left"] = {
                        ["SpellName"] = "Holy Shock",
                        ["SpellID"] = 20473,
                        ["Button"] = 1,
                        ["TargetType"] = "target"
                    }
                }
            }
        }
    }
}
